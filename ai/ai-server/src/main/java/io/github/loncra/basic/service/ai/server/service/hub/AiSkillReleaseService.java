package io.github.loncra.basic.service.ai.server.service.hub;

import io.github.loncra.basic.service.ai.server.config.SkillConfig;
import io.github.loncra.basic.service.ai.server.dao.hub.AiSkillReleaseDao;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillPackageEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillReleaseEntity;
import io.github.loncra.basic.service.resource.api.enumerate.AttachmentTypeEnum;
import io.github.loncra.basic.service.resource.api.service.AttachmentServiceClient;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.ServiceException;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.minio.CopyFileObject;
import io.github.loncra.framework.commons.minio.FileObject;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/**
 *
 * tb_ai_skill_release 的业务逻辑
 *
 * <p>Table: tb_ai_skill_release - Skill 不可变版本</p>
 *
 * @author maurice.chen
 * @see AiSkillReleaseEntity
 * @since 2026-08-04 09:21:08
 */
@Service
@RequiredArgsConstructor
public class AiSkillReleaseService extends BasicService<AiSkillReleaseDao, AiSkillReleaseEntity> {

    public static final String SKILL_RELEASE_OBJECT_PREFIX = "ai/skill/release/";

    private final AttachmentServiceClient attachmentServiceClient;

    private final SkillConfig skillConfig;

    /**
     * 将目录工作区拷贝为一条不可变版本。
     *
     * @return 新 Release 主键
     */
    @Transactional(rollbackFor = Exception.class)
    public Long snapshot(
            AiSkillPackageEntity entity,
            String version,
            String changelog
    ) {
        boolean duplicated = lambdaQuery()
                .eq(AiSkillReleaseEntity::getAiSkillPackageId, entity.getId())
                .eq(AiSkillReleaseEntity::getReleaseVersion, version)
                .exists();
        SystemException.isTrue(!duplicated, () -> new ServiceException("版本 [" + version + "] 已存在"));

        String workPrefix = AiSkillPackageService.SKILL_OBJECT_PREFIX + entity.getId() + AntPathMatcher.DEFAULT_PATH_SEPARATOR;
        List<ObjectWriteResult> files = listWorkspaceFiles(workPrefix);
        SystemException.isTrue(CollectionUtils.isNotEmpty(files), () -> new ServiceException("Skill 目录 [" + entity.getPackageKey() + "] 工作区为空"));
        boolean hasSkillMd = files.stream()
                .map(file -> relativeObjectName(workPrefix, file.getObjectName()))
                .anyMatch(relative -> skillConfig.getFilename().equals(relative));
        SystemException.isTrue(hasSkillMd, () -> new ServiceException("Skill 目录 [" + entity.getPackageKey() + "] 缺少 " + skillConfig.getFilename()));

        AiSkillReleaseEntity release = new AiSkillReleaseEntity();
        release.setAiSkillPackageId(entity.getId());
        release.setReleaseVersion(version);
        release.setChangelog(changelog);
        release.setReleaseTime(Instant.now());
        release.setEnabled(YesOrNo.Yes);
        insert(release);
        SystemException.isTrue(Objects.nonNull(release.getId()), () -> new ServiceException("写入 Skill 版本失败"));

        String releasePrefix = SKILL_RELEASE_OBJECT_PREFIX + release.getId() + AntPathMatcher.DEFAULT_PATH_SEPARATOR;
        try {
            String bucket = AttachmentTypeEnum.SYSTEM_FILE.getValue();
            attachmentServiceClient.copyAttachment(new CopyFileObject(
                    FileObject.of(bucket, workPrefix),
                    FileObject.of(bucket, releasePrefix)
            ));
            release.setContentHash(hashWorkspaceFiles(files, workPrefix));
            updateById(release);
            return release.getId();
        } catch (RuntimeException e) {
            deleteObjectPrefix(releasePrefix);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(
            Collection<? extends Serializable> ids,
            boolean errorThrow,
            boolean useFill
    ) {
        int result = ids.stream().mapToInt(id -> deleteById(id, useFill)).sum();
        if (result != ids.size() && errorThrow) {
            String msg = "删除 id 为 [" + ids + "] 的 [技能版本] 失败";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(
            Serializable id,
            boolean useFill
    ) {
        return deleteByEntity(get(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(
            Collection<AiSkillReleaseEntity> entities,
            boolean errorThrow
    ) {
        int result = entities.stream().mapToInt(this::deleteByEntity).sum();
        if (result != entities.size() && errorThrow) {
            String msg = "删除 id 为 [" + entities.stream().map(AiSkillReleaseEntity::getId).toList() + "] 的 [技能版本] 失败";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(AiSkillReleaseEntity entity) {
        int result = super.deleteByEntity(entity);
        deleteObjectPrefix(SKILL_RELEASE_OBJECT_PREFIX + entity.getId() + AntPathMatcher.DEFAULT_PATH_SEPARATOR);
        return result;
    }

    private List<ObjectWriteResult> listWorkspaceFiles(String workPrefix) {
        List<ObjectWriteResult> items = attachmentServiceClient.findAttachment(
                AttachmentTypeEnum.SYSTEM_FILE.getValue(),
                workPrefix,
                true,
                true
        );
        if (CollectionUtils.isEmpty(items)) {
            return List.of();
        }
        return items.stream()
                .filter(item -> StringUtils.isNotBlank(item.getObjectName()))
                .filter(item -> Strings.CS.startsWith(item.getObjectName(), workPrefix))
                .filter(item -> !Strings.CS.endsWith(item.getObjectName(), AntPathMatcher.DEFAULT_PATH_SEPARATOR))
                .filter(item -> StringUtils.isNotBlank(relativeObjectName(workPrefix, item.getObjectName())))
                .toList();
    }

    private String hashWorkspaceFiles(
            List<ObjectWriteResult> files,
            String workPrefix
    ) {
        MessageDigest digest = DigestUtils.getSha256Digest();
        files.stream()
                .sorted(Comparator.comparing(file -> relativeObjectName(workPrefix, file.getObjectName())))
                .forEach(file -> {
                    String relative = relativeObjectName(workPrefix, file.getObjectName());
                    digest.update(relative.getBytes(StandardCharsets.UTF_8));
                    digest.update((byte) 0);
                    digest.update(StringUtils.defaultString(file.getEtag()).getBytes(StandardCharsets.UTF_8));
                    digest.update((byte) 0);
                });
        return Hex.encodeHexString(digest.digest());
    }

    private String relativeObjectName(
            String workPrefix,
            String objectName
    ) {
        return Strings.CS.removeStart(objectName, workPrefix);
    }

    private void deleteObjectPrefix(String prefix) {
        FileObject fileObject = FileObject.of(AttachmentTypeEnum.SYSTEM_FILE.getValue(), prefix);
        attachmentServiceClient.deleteAttachment(List.of(fileObject), Map.of());
    }
}
