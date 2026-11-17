package io.github.loncra.basic.service.ai.server.service.hub;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.model.FileData;
import io.github.loncra.basic.service.ai.server.config.SkillConfig;
import io.github.loncra.basic.service.ai.server.dao.hub.AiSkillReleaseDao;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillReleaseEntity;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.SourceOfTruthFileSystemFactory;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.ServiceException;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

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

    private final SourceOfTruthFileSystemFactory sourceOfTruthFileSystemFactory;

    private final SkillConfig skillConfig;

    public AbstractFilesystem getSourceOfTruthFileSystem() {
        return sourceOfTruthFileSystemFactory.getSourceOfTruthFileSystem(skillConfig.getReleasePath());
    }

    public List<AiSkillReleaseEntity> findEnabledByPackageId(Long packageId) {
        return lambdaQuery()
                .eq(AiSkillReleaseEntity::getAiSkillPackageId, packageId)
                .eq(AiSkillReleaseEntity::getEnabled, YesOrNo.Yes)
                .orderByDesc(AiSkillReleaseEntity::getId)
                .list();
    }

    public String getStorePath() {
        return skillConfig.getReleasePath();
    }

    public record PathedFileData(String path, FileData fileData) {
        public String content()  { return fileData.content(); }
        public String encoding() { return fileData.encoding(); }
    }

    /**
     * 将目录工作区拷贝为一条不可变版本。
     *
     * @return 新 Release 主键
     */
    @Transactional(rollbackFor = Exception.class)
    public Long snapshot(
            Long packageId,
            String version,
            List<PathedFileData> fileData,
            String changelog
    ) {
        boolean duplicated = lambdaQuery()
                .eq(AiSkillReleaseEntity::getAiSkillPackageId, packageId)
                .eq(AiSkillReleaseEntity::getReleaseVersion, version)
                .exists();
        SystemException.isTrue(!duplicated, () -> new ServiceException("版本 [" + version + "] 已存在"));

        SystemException.isTrue(CollectionUtils.isNotEmpty(fileData), () -> new ServiceException("ID 为 [" + packageId + "] Skill 目录为空"));
        boolean hasSkillMd = fileData.stream()
                .map(file -> Stream.of(StringUtils.split(file.path, AntPathMatcher.DEFAULT_PATH_SEPARATOR)).toList().getLast())
                .anyMatch(relative -> skillConfig.getFilename().equals(relative));
        SystemException.isTrue(hasSkillMd, () -> new ServiceException("ID 为 [" + packageId + "] 缺少 " + skillConfig.getFilename()));

        AiSkillReleaseEntity release = new AiSkillReleaseEntity();
        release.setAiSkillPackageId(packageId);
        release.setReleaseVersion(version);
        release.setChangelog(changelog);
        release.setReleaseTime(Instant.now());
        release.setEnabled(YesOrNo.Yes);
        insert(release);
        SystemException.isTrue(Objects.nonNull(release.getId()), () -> new ServiceException("写入 Skill 版本失败"));
        for (PathedFileData file : fileData) {
            getSourceOfTruthFileSystem().write(RuntimeContext.empty(), file.path(), file.content());
        }
        release.setContentHash(hashWorkspaceFiles(fileData, release.getId()));
        updateById(release);
        return release.getId();
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
        getSourceOfTruthFileSystem().delete(RuntimeContext.empty(), entity.getId().toString());
        //deleteObjectPrefix(skillConfig.getReleasePath() + entity.getId() + AntPathMatcher.DEFAULT_PATH_SEPARATOR);
        return result;
    }

    /*private List<ObjectWriteResult> listWorkspaceFiles(String workPrefix) {
        List<ObjectWriteResult> items = getReleaseSourceOfTruthFileSystem().ls(
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
    }*/

    private String hashWorkspaceFiles(
            List<PathedFileData> files,
            Long releaseId
    ) {
        MessageDigest digest = DigestUtils.getSha256Digest();
        files.stream()
                .sorted(Comparator.comparing(PathedFileData::path))
                .forEach(file -> {
                    String relative = file.path();
                    digest.update(relative.getBytes(StandardCharsets.UTF_8));
                    digest.update((byte) 0);
                    digest.update(StringUtils.defaultString(releaseId.toString()).getBytes(StandardCharsets.UTF_8));
                    digest.update((byte) 0);
                });
        return Hex.encodeHexString(digest.digest());
    }

    /*private String relativeObjectName(
            String workPrefix,
            String objectName
    ) {
        return Strings.CS.removeStart(objectName, workPrefix);
    }*/

    /*private void deleteObjectPrefix(String prefix) {
        FileObject fileObject = FileObject.of(AttachmentTypeEnum.SYSTEM_FILE.getValue(), prefix);
        attachmentServiceClient.deleteAttachment(List.of(fileObject), Map.of(), false);
    }*/
}
