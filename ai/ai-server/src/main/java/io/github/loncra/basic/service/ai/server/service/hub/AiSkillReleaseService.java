package io.github.loncra.basic.service.ai.server.service.hub;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.skill.util.SkillUtil;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.model.FileInfo;
import io.agentscope.harness.agent.filesystem.model.GlobResult;
import io.agentscope.harness.agent.filesystem.model.ReadResult;
import io.github.loncra.basic.service.ai.server.config.SkillConfig;
import io.github.loncra.basic.service.ai.server.dao.hub.AiSkillReleaseDao;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillReleaseEntity;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.SourceOfTruthFileSystemFactory;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.ServiceException;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.commons.jackson.serializer.DesensitizeSerializer;
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

    /**
     * 按 release 主键读取快照内容，构造 agentScope 技能。
     *
     * <p>落盘布局：{@code <releasePath>/<packageId>/<version>/<相对路径>}（版本号取自
     * release 实体的 {@code releaseVersion}）；SKILL.md 的 YAML frontmatter
     * （name/description 必填）由 {@link SkillUtil#createFrom} 解析，其余文件作为技能资源内嵌。
     *
     * @param releaseId 版本主键
     * @return 构造完成的技能
     */
    public IdValueMetadata<Long, String> loadSkill(Long releaseId) {
        AiSkillReleaseEntity release = get(releaseId);
        SystemException.isTrue(Objects.nonNull(release), () -> new ServiceException("Skill 版本不存在: " + releaseId));
        SystemException.isTrue(YesOrNo.Yes.equals(release.getEnabled()), () -> new ServiceException("Skill 版本已停用: " + releaseId));

        AbstractFilesystem filesystem = getSourceOfTruthFileSystem();
        String releasePrefix = String.join(
                AntPathMatcher.DEFAULT_PATH_SEPARATOR,
                release.getAiSkillPackageId().toString(),
                release.getReleaseVersion(),
                StringUtils.EMPTY
        );

        GlobResult glob = filesystem.glob(RuntimeContext.empty(), DesensitizeSerializer.DEFAULT_DESENSITIZE_SYMBOL, releasePrefix);
        SystemException.isTrue(glob.isSuccess(), () -> new ServiceException("Skill 版本文件列举失败: " + glob.error()));

        Map<String, Object> resources = new LinkedHashMap<>();
        String skillContent = null;
        for (FileInfo file : glob.matches()) {
            if (Strings.CS.endsWith(file.path(), AntPathMatcher.DEFAULT_PATH_SEPARATOR)) {
                continue;
            }
            ReadResult read = filesystem.read(RuntimeContext.empty(), file.path(), 0, 0);
            if (!read.isSuccess() || Objects.isNull(read.fileData())) {
                continue;
            }
            String relative = Strings.CS.removeStart(file.path(), AntPathMatcher.DEFAULT_PATH_SEPARATOR + releasePrefix);
            if (skillConfig.getFilename().equals(relative)) {
                skillContent = read.fileData().content();
            } else {
                resources.put(relative, read.fileData().content());
            }
        }
        SystemException.isTrue(
                StringUtils.isNotBlank(skillContent),
                () -> new ServiceException("Skill 版本缺少 " + skillConfig.getFilename() + ": " + releaseId)
        );
        IdValueMetadata<Long, String> metadata = IdValueMetadata.of(releaseId, skillContent);
        metadata.setMetadata(resources);
        return metadata;
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
            List<SourceOfTruthFileSystemFactory.PathFileData> fileData,
            String changelog
    ) {
        boolean duplicated = lambdaQuery()
                .eq(AiSkillReleaseEntity::getAiSkillPackageId, packageId)
                .eq(AiSkillReleaseEntity::getReleaseVersion, version)
                .exists();
        SystemException.isTrue(!duplicated, () -> new ServiceException("版本 [" + version + "] 已存在"));

        SystemException.isTrue(CollectionUtils.isNotEmpty(fileData), () -> new ServiceException("ID 为 [" + packageId + "] Skill 目录为空"));
        boolean hasSkillMd = fileData.stream()
                .map(file -> Stream.of(StringUtils.split(file.path(), AntPathMatcher.DEFAULT_PATH_SEPARATOR)).toList().getLast())
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
        for (SourceOfTruthFileSystemFactory.PathFileData file : fileData) {
            String relativePath = Strings.CS.removeStart(file.path(), AntPathMatcher.DEFAULT_PATH_SEPARATOR + packageId + AntPathMatcher.DEFAULT_PATH_SEPARATOR);
            String releasePath = String.join(AntPathMatcher.DEFAULT_PATH_SEPARATOR, packageId.toString(), version, relativePath);
            getSourceOfTruthFileSystem().write(RuntimeContext.empty(), releasePath, file.content());
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
        return result;
    }

    private String hashWorkspaceFiles(
            List<SourceOfTruthFileSystemFactory.PathFileData> files,
            Long releaseId
    ) {
        MessageDigest digest = DigestUtils.getSha256Digest();
        files.stream()
                .sorted(Comparator.comparing(SourceOfTruthFileSystemFactory.PathFileData::path))
                .forEach(file -> {
                    String relative = file.path();
                    digest.update(relative.getBytes(StandardCharsets.UTF_8));
                    digest.update((byte) 0);
                    digest.update(StringUtils.defaultString(releaseId.toString()).getBytes(StandardCharsets.UTF_8));
                    digest.update((byte) 0);
                });
        return Hex.encodeHexString(digest.digest());
    }
}

