package io.github.loncra.basic.service.ai.server.service.hub;

import com.fasterxml.jackson.core.util.VersionUtil;
import io.github.loncra.basic.service.ai.api.constants.AiConstants;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.PluginPackageMetadata;
import io.github.loncra.basic.service.ai.server.dao.hub.AiSkillPackageDao;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillPackageEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillReleaseEntity;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.enumerate.DataStatusEnum;
import io.github.loncra.basic.service.resource.api.enumerate.AttachmentTypeEnum;
import io.github.loncra.basic.service.resource.api.service.AttachmentServiceClient;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import io.github.loncra.framework.commons.exception.ServiceException;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.minio.FileObject;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.AntPathMatcher;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * tb_ai_skill_package 的业务逻辑
 *
 * <p>Table: tb_ai_skill_package - Skill 目录</p>
 *
 * @author maurice.chen
 * @see AiSkillPackageEntity
 * @since 2026-08-04 09:21:08
 */
@Service
@RequiredArgsConstructor
public class AiSkillPackageService extends BasicService<AiSkillPackageDao, AiSkillPackageEntity> {

    public static final String SKILL_OBJECT_PREFIX = "ai/skill/";

    private final AiSkillReleaseService aiSkillReleaseService;

    private final AmqpTemplate amqpTemplate;

    private final AttachmentServiceClient attachmentServiceClient;

    @Transactional(rollbackFor = Exception.class)
    public void release(List<Long> ids) {
        ids.forEach(id -> lambdaUpdate().set(PluginPackageMetadata::getStatus, DataStatusEnum.RELEASE.getValue())
                .eq(PluginPackageMetadata::getId, id)
                .update()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void revoke(List<Long> ids) {
        ids.forEach(id -> lambdaUpdate().set(PluginPackageMetadata::getStatus, DataStatusEnum.REVOKE.getValue())
                .eq(PluginPackageMetadata::getId, id)
                .update()
        );
    }

    /**
     * 将工作区拷贝为一条不可变 Release，并更新 {@link AiSkillPackageEntity#getLatestVersion()}。
     * 不改变目录上架 {@link #release} 状态。
     *
     * @return 新 Release 主键
     */
    @Transactional(rollbackFor = Exception.class)
    public Long snapshot(
            Long packageId,
            String releaseVersion,
            String changelog
    ) {
        String version = StringUtils.trimToEmpty(releaseVersion);
        SystemException.isTrue(
                !VersionUtil.parseVersion(version, null, null).isUnknownVersion(),
                () -> new ServiceException("版本号非法")
        );
        AiSkillPackageEntity entity = get(packageId);
        SystemException.isTrue(Objects.nonNull(entity), () -> new ServiceException("找不到 ID 为 [" + packageId + "] 的 Skill 目录"));
        SystemException.isTrue(
                ExecuteStatus.Success.equals(entity.getExecuteStatus()),
                () -> new ServiceException("Skill 目录 [" + entity.getPackageKey() + "] 尚未摄取成功，不能打包")
        );
        Long releaseId = aiSkillReleaseService.snapshot(entity, version, changelog);
        lambdaUpdate()
                .set(AiSkillPackageEntity::getLatestVersion, version)
                .eq(PluginPackageMetadata::getId, packageId)
                .update();
        return releaseId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(AiSkillPackageEntity entity) {
        entity.setStatus(DataStatusEnum.NEW);
        entity.setExecuteStatus(ExecuteStatus.Pending);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                amqpTemplate.convertAndSend(
                        SystemConstants.SYS_AI_RABBITMQ_EXCHANGE,
                        AiConstants.MQ_SKILL_SOURCE_INGEST_QUEUE,
                        entity.getId()
                );
            }
        });
        return super.insert(entity);
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
            String msg = "删除 id 为 [" + ids + "] 的 [技能信息] 失败";
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
            Collection<AiSkillPackageEntity> entities,
            boolean errorThrow
    ) {
        int result = entities.stream().mapToInt(this::deleteByEntity).sum();
        if (result != entities.size() && errorThrow) {
            String msg = "删除 id 为 [" + entities.stream().map(AiSkillPackageEntity::getId).toList() + "] 的 [技能信息] 失败";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(AiSkillPackageEntity entity) {
        List<AiSkillReleaseEntity> releases = aiSkillReleaseService.lambdaQuery()
                .eq(AiSkillReleaseEntity::getAiSkillPackageId, entity.getId())
                .list();
        int result = super.deleteByEntity(entity);
        if (CollectionUtils.isNotEmpty(releases)) {
            aiSkillReleaseService.deleteById(releases.stream().map(AiSkillReleaseEntity::getId).toList());
        }
        FileObject fileObject = FileObject.of(
                AttachmentTypeEnum.SYSTEM_FILE.getValue(),
                SKILL_OBJECT_PREFIX + entity.getId() + AntPathMatcher.DEFAULT_PATH_SEPARATOR
        );
        attachmentServiceClient.deleteAttachment(List.of(fileObject), Map.of());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void reingest(List<Long> ids) {
        List<AiSkillPackageEntity> skills = get(ids).stream()
                .filter(Objects::nonNull)
                .filter(s -> ExecuteStatus.PENDING_STATUS.contains(s.getExecuteStatus()))
                .toList();
        for (AiSkillPackageEntity skill : skills) {
            lambdaUpdate()
                    .set(AiSkillPackageEntity::getExecuteStatus, ExecuteStatus.Pending.getValue())
                    .eq(AiSkillPackageEntity::getId, skill.getId())
                    .update();

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    amqpTemplate.convertAndSend(
                            SystemConstants.SYS_AI_RABBITMQ_EXCHANGE,
                            AiConstants.MQ_SKILL_SOURCE_INGEST_QUEUE,
                            skill.getId()
                    );
                }
            });
        }
    }
}
