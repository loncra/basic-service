package io.github.loncra.basic.service.ai.server.service.hub;

import io.github.loncra.basic.service.ai.api.constants.AiConstants;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.PluginPackageMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.SkillSourceTypeEnum;
import io.github.loncra.basic.service.ai.server.dao.hub.AiSkillPackageDao;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillPackageEntity;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.enumerate.DataStatusEnum;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;

/**
 *
 * tb_ai_skill_package 的业务逻辑
 *
 * <p>Table: tb_ai_skill_package - Skill 目录</p>
 *
 * @see AiSkillPackageEntity
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@Service
@RequiredArgsConstructor
public class AiSkillPackageService extends BasicService<AiSkillPackageDao, AiSkillPackageEntity> {

    private final AiSkillReleaseService aiSkillReleaseService;

    private final AmqpTemplate amqpTemplate;

    @Transactional(rollbackFor = Exception.class)
    public void release(List<Long> ids) {
        ids.forEach(id -> lambdaUpdate().set(PluginPackageMetadata::getStatus, DataStatusEnum.RELEASE.getValue())
                .eq(PluginPackageMetadata::getId,id)
                .update()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void revoke(List<Long> ids) {
        ids.forEach(id -> lambdaUpdate().set(PluginPackageMetadata::getStatus, DataStatusEnum.REVOKE.getValue())
                .eq(PluginPackageMetadata::getId,id)
                .update()
        );
    }

    /**
     * 从来源物化目录树并写入一条不可变 Release（与目录上架 {@link #release} 无关）。
     *
     * @return 新 Release 主键
     */
    /*@Transactional(rollbackFor = Exception.class)
    public Long snapshot(Long packageId, String releaseVersion, String changelog) {
        String version = StringUtils.trimToEmpty(releaseVersion);
        SystemException.isTrue(
                !VersionUtil.parseVersion(version, null, null).isUnknownVersion(),
                () -> new ServiceException("版本号非法")
        );

        AiSkillPackageEntity entity = get(packageId);
        SystemException.isTrue(Objects.nonNull(entity), () -> new ServiceException("找不到 ID 为 [" + packageId + "] 的 Skill 目录"));
        SystemException.isTrue(Objects.nonNull(entity.getSourceType()), () -> new ServiceException("Skill 目录 [" + entity.getPackageKey() + "] 未配置来源类型"));
        boolean duplicated = aiSkillReleaseService.lambdaQuery()
                .eq(AiSkillReleaseEntity::getAiSkillPackageId, packageId)
                .eq(AiSkillReleaseEntity::getReleaseVersion, version)
                .exists();
        SystemException.isTrue(!duplicated, () -> new ServiceException("版本 [" + version + "] 已存在"));
        SkillPackageMetadata packageMetadata = entity.obtainMetadata();
        AbstractSkillSourceMetadata source = Objects.isNull(packageMetadata) ? null : packageMetadata.obtainSource();
        SystemException.isTrue(Objects.nonNull(source), () -> new ServiceException("Skill 目录 [" + entity.getPackageKey() + "] 缺少来源配置"));
        SkillSourceResolver resolver = skillSourceResolvers.stream()
                .filter(item -> item.isSupport(entity.getSourceType().toString()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到来源类型为 [" + entity.getSourceType() + "] 的解析器"));

        Path local = resolver.materialize(entity.getPackageKey(), source);
        try {
            String contentHash = hashTree(local);
            String storagePrefix = "skill/" + packageId + "/" + version + "/";
            skillObjectStorage.uploadDirectory(local, storagePrefix);

            ObjectSkillReleaseStorageMetadata storage = new ObjectSkillReleaseStorageMetadata();
            storage.setPrefix(storagePrefix);
            storage.setBucket(skillObjectStorage.systemFileBucket());

            Map<String, Object> storageMap = new LinkedHashMap<>(CastUtils.convertValue(storage, CastUtils.MAP_TYPE_REFERENCE));
            storageMap.put(TypeIdNameMetadata.TYPE_FIELD_NAME, storage.getType());

            AiSkillReleaseEntity release = new AiSkillReleaseEntity();
            release.setAiSkillPackageId(packageId);
            release.setReleaseVersion(version);
            release.setContentHash(contentHash);
            release.setStorage(storageMap);
            release.setChangelog(changelog);
            release.setReleaseTime(Instant.now());
            release.setEnabled(YesOrNo.Yes);
            aiSkillReleaseService.insert(release);

            lambdaUpdate()
                    .set(AiSkillPackageEntity::getLatestVersion, version)
                    .eq(PluginPackageMetadata::getId, packageId)
                    .update();
            return release.getId();
        }
        finally {
            skillObjectStorage.deleteQuietly(local);
        }
    }

    private String hashTree(Path root) {
        MessageDigest digest = DigestUtils.getSha256Digest();
        List<Path> files = SystemException.convertSupplier(
                () -> {
                    try (var walk = Files.walk(root)) {
                        return walk.filter(Files::isRegularFile)
                                .sorted(Comparator.comparing(path -> toUnixPath(root.relativize(path))))
                                .toList();
                    }
                },
                "[skill] 计算内容指纹失败"
        );
        for (Path file : files) {
            String relative = toUnixPath(root.relativize(file));
            digest.update(relative.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            SystemException.convertRunnable(
                    () -> {
                        try (InputStream inputStream = Files.newInputStream(file)) {
                            DigestUtils.updateDigest(digest, inputStream);
                        }
                    },
                    "[skill] 计算内容指纹失败: " + relative
            );
        }
        return Hex.encodeHexString(digest.digest());
    }

    private String toUnixPath(Path relative) {
        return Strings.CS.replace(relative.toString(), "\\", AntPathMatcher.DEFAULT_PATH_SEPARATOR);
    }*/

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(AiSkillPackageEntity entity) {
        entity.setStatus(DataStatusEnum.NEW);
        if (SkillSourceTypeEnum.GIT.equals(entity.getSourceType())) {
            entity.setExecuteStatus(ExecuteStatus.Pending);
        }
        else {
            entity.setExecuteStatus(ExecuteStatus.Success);
        }
        int rows = super.insert(entity);
        publishSourceIngest(entity);
        return rows;
    }

    private void publishSourceIngest(AiSkillPackageEntity entity) {
        if (!SkillSourceTypeEnum.GIT.equals(entity.getSourceType()) || Objects.isNull(entity.getId())) {
            return;
        }
        Long packageId = entity.getId();
        Runnable send = () -> amqpTemplate.convertAndSend(
                SystemConstants.SYS_AI_RABBITMQ_EXCHANGE,
                AiConstants.MQ_SKILL_SOURCE_INGEST_QUEUE,
                packageId
        );
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send.run();
                }
            });
            return;
        }
        send.run();
    }
}
