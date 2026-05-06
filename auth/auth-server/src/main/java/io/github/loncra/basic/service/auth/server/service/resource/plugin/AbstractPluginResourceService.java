package io.github.loncra.basic.service.auth.server.service.resource.plugin;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceCastegoryEnum;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.config.AuthAppConfig;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.domain.metdata.ResourceMetadata;
import io.github.loncra.basic.service.auth.server.domain.metdata.SyncPluginResourceMetadata;
import io.github.loncra.basic.service.auth.server.service.resource.ResourceService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.id.number.NumberIdEntity;
import io.github.loncra.framework.mybatis.plus.baisc.VersionEntity;
import io.github.loncra.framework.security.plugin.PluginInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 抽象的 redis 缓存 插件资源服务实现
 *
 * @author maurice.chen
 */
@Slf4j
@Getter
@Setter(onMethod_ = @Autowired)
public abstract class AbstractPluginResourceService implements PluginResourceService {

    public static final String COMMONS_APPLICATION_NAME = "commons";

    private AuthAppConfig authAppConfig;

    private ResourceService resourceService;

    /**
     * 创建資源
     *
     * @param plugin   插件信息
     * @param consumer 自定义内容
     * @return 新的資源
     */
    protected ResourceMetadata createResource(
            PluginInfo plugin,
            Consumer<ResourceMetadata> consumer
    ) {
        ResourceMetadata target = CastUtils.of(
                plugin,
                ResourceMetadata.class,
                IdEntity.ID_FIELD_NAME,
                PluginInfo.DEFAULT_CHILDREN_NAME,
                PluginInfo.DEFAULT_SOURCES_NAME
        );

        Assert.notEmpty(plugin.getSources(), "插件实体 [" + CastUtils.convertValue(plugin, Map.class) + "] 的 sources 为空");

        List<ResourceSourceEnum> sources = plugin.getSources()
                .stream()
                .map(s -> NameEnum.ofEnum(ResourceSourceEnum.class, s))
                .collect(Collectors.toList());

        target.setSources(sources);
        target.setType(ValueEnum.ofEnum(ResourceTypeEnum.class, plugin.getType()));
        target.setCode(plugin.getId());

        if (Strings.CS.equals(plugin.getParent(), PluginInfo.DEFAULT_ROOT_PARENT_NAME)) {
            target.setParentId(null);
        }

        consumer.accept(target);

        // 设置 target 变量的子节点
        plugin.getChildren()
                .stream()
                .map(c -> createResource(CastUtils.cast(c, PluginInfo.class), consumer))
                //.peek(c -> c.setParentId())
                .forEach(r -> target.getChildren().add(r));

        return target;
    }

    /**
     * 比对并更新缓存信息
     *
     * @param version           版本号
     * @param newResourceList   新资源
     * @param existingResources 当前资源
     */
    protected List<ResourceEntity> compareThenMerge(
            Version version,
            List<ResourceMetadata> newResourceList,
            List<ResourceEntity> existingResources
    ) {
        List<ResourceEntity> result = new LinkedList<>();
        Map<String, Long> codeIdMap = new LinkedHashMap<>();
        for (ResourceMetadata newResource : newResourceList) {
            // 查找是否存在相同 ID 的资源
            Optional<ResourceEntity> optional = existingResources
                    .stream()
                    .filter(r -> r.getCode().equals(newResource.getCode()))
                    .map(c -> CastUtils.cast(c, ResourceEntity.class))
                    .findFirst();

            ResourceEntity newResourceEntity = CastUtils.of(newResource, ResourceEntity.class);
            newResourceEntity.setCategory(ResourceCastegoryEnum.PLUGIN);
            if (optional.isPresent()) {
                ResourceEntity existing = optional.get();
                // 比较版本号，只有当新版本大于现有版本时才更新
                if (StringUtils.isNotBlank(existing.getVersion()) && StringUtils.isNotBlank(newResourceEntity.getVersion())) {
                    updateResource(version, newResource, existing);
                }
                // 版本号为空，直接替换
                else {
                    BeanUtils.copyProperties(newResource, existing, IdEntity.ID_FIELD_NAME, VersionEntity.VERSION_FIELD_NAME, NumberIdEntity.CREATION_TIME_FIELD_NAME);
                    resourceService.updateById(existing);
                }
                result.add(existing);
                codeIdMap.put(existing.getCode(), existing.getId());
            } else if (resourceService.getByCode(newResourceEntity.getCode()) == null){
                // 不存在相同 ID 的资源，直接添加
                resourceService.insert(newResourceEntity);
                result.add(newResourceEntity);
                codeIdMap.put(newResourceEntity.getCode(), newResourceEntity.getId());
            }

            if (CollectionUtils.isNotEmpty(newResource.getChildren())) {
                List<ResourceMetadata> newChildrenResourceList = newResource.getChildren()
                        .stream()
                        .map(c -> CastUtils.cast(c, ResourceMetadata.class))
                        .peek(c -> c.setParentId(codeIdMap.get(newResource.getCode())))
                        .collect(Collectors.toCollection(LinkedList::new));

                result.addAll(compareThenMerge(version, newChildrenResourceList, existingResources));
            }
        }

        return result;
    }

    private void updateResource(
            Version existingVersion,
            ResourceMetadata newResource,
            ResourceEntity existing
    ) {
        try {

            Version newVersion = VersionUtil.parseVersion(newResource.getVersion(), existingVersion.getGroupId(), existingVersion.getArtifactId());

            int compare = newVersion.compareTo(existingVersion);
            if (compare > 0) {
                // 新版本大于现有版本，移除旧资源并添加新资源
                BeanUtils.copyProperties(newResource, existing, IdEntity.ID_FIELD_NAME, VersionEntity.VERSION_FIELD_NAME, NumberIdEntity.CREATION_TIME_FIELD_NAME);
                existing.setEnabled(YesOrNo.Yes);
                resourceService.updateById(existing);
                if (log.isDebugEnabled()) {
                    log.debug(
                            "资源 [{}] 的版本号 [{}] 大于现有版本 [{}]，已更新",
                            newResource.getCode(), newResource.getVersion(), existing.getVersion()
                    );
                }
            } else if (compare == 0) {
                // 版本号相等，比较内容哈希
                String existingHash = buildHash(existing);
                String newHash = buildHash(newResource);
                if (!Objects.equals(existingHash, newHash)) {
                    BeanUtils.copyProperties(newResource, existing, IdEntity.ID_FIELD_NAME, VersionEntity.VERSION_FIELD_NAME, NumberIdEntity.CREATION_TIME_FIELD_NAME);
                    existing.setEnabled(YesOrNo.Yes);
                    resourceService.updateById(existing);
                    if (log.isDebugEnabled()) {
                        log.debug("资源 [{}] 版本相等但内容变化，已更新缓存", newResource.getCode());
                    }
                }
            } else if (log.isDebugEnabled()) {
                log.debug(
                        "资源 [{}] 的版本号 [{}] 不大于现有版本 [{}]，跳过更新",
                        newResource.getCode(), newResource.getVersion(), existing.getVersion()
                );
            }
        } catch (Exception e) {
            log.warn("解析资源 [{}] 的版本号失败，将使用新资源: {}", newResource.getCode(), e.getMessage());
            // 解析失败时，移除旧资源并添加新资源
            BeanUtils.copyProperties(newResource, existing, IdEntity.ID_FIELD_NAME, VersionEntity.VERSION_FIELD_NAME, NumberIdEntity.CREATION_TIME_FIELD_NAME);
            existing.setEnabled(YesOrNo.Yes);
            resourceService.updateById(existing);
        }
    }

    /**
     * 计算对象哈希（用于内容变更检测）
     */
    private String buildHash(Object target) {
        try {
            ResourceMetadata metadata = CastUtils.of(target, ResourceMetadata.class, PluginInfo.DEFAULT_CHILDREN_NAME);
            byte[] bytes = CastUtils.getObjectMapper()
                    .writeValueAsBytes(metadata);
            return DigestUtils.md5DigestAsHex(bytes);
        } catch (Exception e) {
            log.warn("计算资源内容哈希失败，将视为内容变化: {}", e.getMessage());
            return DigestUtils.md5DigestAsHex(Objects.toString(UUID.randomUUID()).getBytes(StandardCharsets.UTF_8));
        }
    }


    @Override
    public List<ResourceEntity> getResourcesStream(
            Set<Long> resourceIds,
            ResourceSourceEnum... sources
    ) {

        if (CollectionUtils.isEmpty(resourceIds)) {
            return new LinkedList<>();
        }

        List<ResourceSourceEnum> sourceList = Arrays.asList(sources);
        return resourceIds.stream()
                .map(resourceService::get)
                .filter(r -> r.getSources().stream().anyMatch(sourceList::contains))
                .distinct()
                .toList();
    }

    /**
     * 获取资源集合
     *
     * @return 资源集合
     */
    @Override
    public List<ResourceEntity> getResources() {
        return resourceService.lambdaQuery().eq(ResourceEntity::getEnabled, YesOrNo.Yes.getValue()).list();
    }

    /**
     * 获取插件服务名称集合
     *
     * @return 插件服务名称集合
     */
    @Override
    public Set<String> getPluginServerNames() {
        return getResources().stream()
                .collect(Collectors.groupingBy(ResourceMetadata::getApplicationName))
                .keySet();
    }

    /**
     * 获取资源集合
     *
     * @param applicationName 应用名称
     * @param sources         符合来源的记录
     * @return 资源集合
     */
    @Override
    public List<ResourceEntity> getResources(
            String applicationName,
            ResourceSourceEnum... sources
    ) {
        Stream<ResourceEntity> stream = getResources().stream();

        if (StringUtils.isNotBlank(applicationName)) {
            stream = stream.filter(r -> r.getApplicationName().equals(applicationName));
        }

        if (ArrayUtils.isNotEmpty(sources)) {
            List<ResourceSourceEnum> sourceList = Arrays.asList(sources);
            stream = stream.filter(r -> r.getSources().stream().anyMatch(sourceList::contains));
        }

        return stream.toList();
    }

    protected SyncPluginResourceMetadata updateResourceMetadata(
            Version version,
            List<ResourceMetadata> newResourceList,
            String... applicationName
    ) {
        List<String> applicationNames = Arrays.asList(applicationName);
        // 获取现有资源
        List<ResourceEntity> existingResources = getResources().stream()
                .filter(r -> applicationNames.contains(r.getApplicationName()))
                .collect(Collectors.toList());

        // 比较数据信息，并更新或添加新资源到缓存
        List<ResourceEntity> result = compareThenMerge(version, newResourceList, existingResources);

        // 移除该应用的其他旧资源（不在新资源列表中的）
        List<String> newResourceCodes = result
                .stream()
                .map(ResourceMetadata::getCode)
                .distinct()
                .toList();

        List<Long> deleteIds = existingResources.stream()
                .filter(r -> !newResourceCodes.contains(r.getCode()))
                .map(ResourceEntity::getId)
                .toList();

        deleteIds.forEach(resourceService::deleteById);

        SyncPluginResourceMetadata metadata = new SyncPluginResourceMetadata();
        metadata.setResources(result);
        metadata.setDeleteIds(deleteIds);
        metadata.setApplicationNames(applicationNames);

        return metadata;
    }

    public record SyncPluginResourceEvent (SyncPluginResourceMetadata dto) {}
}
