package io.github.loncra.basic.service.auth.server.service.plugin;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import io.github.loncra.basic.service.auth.server.config.AuthAppConfig;
import io.github.loncra.basic.service.auth.server.domain.metdata.ResourceMetadata;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import io.github.loncra.framework.commons.tree.TreeUtils;
import io.github.loncra.framework.security.plugin.PluginInfo;
import io.github.loncra.framework.spring.security.core.plugin.metadata.IdResourceAuthorityMetadata;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
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
public abstract class AbstractRedissonPluginResourceService implements PluginResourceService {

    private RedissonClient redissonClient;

    private AuthAppConfig authAppConfig;

    /**
     * 获取 Redis 资源列表
     *
     * @return Redis 资源列表
     */
    protected RList<ResourceMetadata> getRedisResourceList() {
        return redissonClient.getList(authAppConfig.getPluginResourceCache().getName());
    }

    /**
     * 创建資源
     *
     * @param plugin   插件信息
     * @param parent   夫类资源
     * @param consumer 自定义内容
     *
     * @return 新的資源
     */
    protected ResourceMetadata createResource(
            PluginInfo plugin,
            ResourceMetadata parent,
            Consumer<ResourceMetadata> consumer
    ) {
        ResourceMetadata target = CastUtils.of(
                plugin,
                ResourceMetadata.class,
                PluginInfo.DEFAULT_CHILDREN_NAME,
                PluginInfo.DEFAULT_SOURCES_NAME
        );

        Assert.notEmpty(plugin.getSources(), "插件实体 [" + CastUtils.convertValue(plugin, Map.class) + "] 的 sources 为空");

        List<ResourceSourceEnum> sources = plugin.getSources()
                .stream()
                .map(s -> NameEnum.ofEnum(ResourceSourceEnum.class, s))
                .collect(Collectors.toList());

        target.setSources(sources);
        target.setType(plugin.getType());

        if (Strings.CS.equals(plugin.getParent(), PluginInfo.DEFAULT_ROOT_PARENT_NAME)) {
            target.setParentId(null);
        }

        else if (Objects.nonNull(parent)) {
            target.setParentId(parent.getId());
        }

        consumer.accept(target);

        // 设置 target 变量的子节点
        plugin.getChildren()
                .stream()
                .map(c -> createResource(CastUtils.cast(c, PluginInfo.class), target, consumer))
                .forEach(r -> target.getChildren().add(r));

        return target;
    }

    /**
     * 比对并更新缓存信息
     *
     * @param version             版本号
     * @param unmergeResourceList 解除合并树形后的新资源
     * @param existingResources   缓存资源
     */
    protected void compareThenUpdateCache(
            Version version,
            List<ResourceMetadata> unmergeResourceList,
            List<ResourceMetadata> existingResources
    ) {
        for (ResourceMetadata newResource : unmergeResourceList) {
            // 查找是否存在相同 ID 的资源
            Optional<ResourceMetadata> existingResource = existingResources
                    .stream()
                    .filter(r -> r.getId().equals(newResource.getId()))
                    .findFirst();

            if (existingResource.isPresent()) {
                ResourceMetadata existing = existingResource.get();
                // 比较版本号，只有当新版本大于现有版本时才更新
                if (StringUtils.isNotBlank(existing.getVersion()) && StringUtils.isNotBlank(newResource.getVersion())) {
                    updateCache(version, newResource, existing, existingResources);
                }
                // 版本号为空，直接替换
                else {
                    existingResources.remove(existing);
                    existingResources.add(newResource);
                }
            }
            else {
                // 不存在相同 ID 的资源，直接添加
                existingResources.add(newResource);
            }
        }

    }

    private void updateCache(
            Version existingVersion,
            ResourceMetadata newResource,
            ResourceMetadata existing,
            List<ResourceMetadata> existingResources
    ) {
        try {

            Version newVersion = VersionUtil.parseVersion(newResource.getVersion(), existingVersion.getGroupId(), existingVersion.getArtifactId());

            int compare = newVersion.compareTo(existingVersion);
            if (compare > 0) {
                // 新版本大于现有版本，移除旧资源并添加新资源
                existingResources.remove(existing);
                existingResources.add(newResource);
                if (log.isDebugEnabled()) {
                    log.debug("资源 [{}] 的版本号 [{}] 大于现有版本 [{}]，已更新",
                              newResource.getId(), newResource.getVersion(), existing.getVersion());
                }
            }
            else if (compare == 0) {
                // 版本号相等，比较内容哈希
                String existingHash = buildHash(existing);
                String newHash = buildHash(newResource);
                if (!Objects.equals(existingHash, newHash)) {
                    existingResources.remove(existing);
                    existingResources.add(newResource);
                    if (log.isDebugEnabled()) {
                        log.debug("资源 [{}] 版本相等但内容变化，已更新缓存", newResource.getId());
                    }
                }
            }
            else if (log.isDebugEnabled()) {
                log.debug("资源 [{}] 的版本号 [{}] 不大于现有版本 [{}]，跳过更新",
                          newResource.getId(), newResource.getVersion(), existing.getVersion());
            }
        }
        catch (Exception e) {
            log.warn("解析资源 [{}] 的版本号失败，将使用新资源: {}", newResource.getId(), e.getMessage());
            // 解析失败时，移除旧资源并添加新资源
            existingResources.remove(existing);
            existingResources.add(newResource);
        }
    }

    /**
     * 计算对象哈希（用于内容变更检测）
     */
    private String buildHash(Object target) {
        try {
            byte[] bytes = CastUtils.getObjectMapper()
                    .writeValueAsBytes(target);
            return DigestUtils.md5DigestAsHex(bytes);
        }
        catch (Exception e) {
            log.warn("计算资源内容哈希失败，将视为内容变化: {}", e.getMessage());
            return DigestUtils.md5DigestAsHex(Objects.toString(UUID.randomUUID()).getBytes(StandardCharsets.UTF_8));
        }
    }



    @Override
    public List<ResourceMetadata> getResourcesStream(
            List<IdResourceAuthorityMetadata> resources,
            ResourceSourceEnum... sources
    ) {

        if (CollectionUtils.isEmpty(resources)) {
            return new LinkedList<>();
        }

        List<ResourceSourceEnum> sourceList = Arrays.asList(sources);

        List<ResourceMetadata> result = new LinkedList<>();
        Map<String, List<String>> resourceMap = resources.stream()
                .collect(Collectors.groupingBy(IdResourceAuthorityMetadata::getApplicationName, Collectors.mapping(IdResourceAuthorityMetadata::getId, Collectors.toList())));
        for (Map.Entry<String, List<String>> entry : resourceMap.entrySet()) {
            List<ResourceMetadata> data = getResources(entry.getKey());

            List<ResourceMetadata> findResources = data
                    .stream()
                    .filter(r -> entry.getValue().contains(r.getId()))
                    .filter(r -> r.getSources().stream().anyMatch(sourceList::contains))
                    .toList();

            result.addAll(findResources);
        }

        return result.stream()
                .distinct()
                .toList();
    }



    /**
     * 获取资源集合
     *
     * @return 资源集合
     */
    @Override
    public List<ResourceMetadata> getResources() {
        RList<ResourceMetadata> redisResourceList = getRedisResourceList();
        return redisResourceList.readAll()
                .stream()
                .map(r -> CastUtils.of(r, ResourceMetadata.class, PluginInfo.DEFAULT_CHILDREN_NAME))
                .collect(Collectors.toList());
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
     *
     * @return 资源集合
     */
    @Override
    public List<ResourceMetadata> getResources(
            String applicationName,
            ResourceSourceEnum... sources
    ) {
        Stream<ResourceMetadata> stream = getResources().stream();

        if (StringUtils.isNotBlank(applicationName)) {
            stream = stream.filter(r -> r.getApplicationName().equals(applicationName));
        }

        if (ArrayUtils.isNotEmpty(sources)) {
            List<ResourceSourceEnum> sourceList = Arrays.asList(sources);
            stream = stream.filter(r -> r.getSources().stream().anyMatch(sourceList::contains));
        }

        return stream.toList();
    }

    protected List<ResourceMetadata> updateResourceMetadata(
            Version version,
            List<ResourceMetadata> newResourceList,
            String applicationName
    ) {
        List<ResourceMetadata> unmergeNewResourceList = TreeUtils.unBuildGenericTree(newResourceList);

        // 从 Redis 获取现有资源
        RList<ResourceMetadata> redisResourceList = getRedisResourceList();
        List<ResourceMetadata> existingResources = new ArrayList<>(redisResourceList.readAll());

        // 比较数据信息，并更新或添加新资源到缓存
        compareThenUpdateCache(version, unmergeNewResourceList, existingResources);

        // 移除该应用的其他旧资源（不在新资源列表中的）
        List<String> newResourceIds = unmergeNewResourceList
                .stream()
                .map(ResourceMetadata::getId)
                .distinct()
                .toList();
        existingResources.removeIf(r -> r.getApplicationName().equals(applicationName)&& !newResourceIds.contains(r.getId()));

        List<ResourceMetadata> distinctList = existingResources.stream()
                .distinct()
                .toList();
        // 更新 Redis 缓存
        redisResourceList.clear();
        if (CollectionUtils.isNotEmpty(distinctList)) {
            redisResourceList.addAll(distinctList);
        }

        return unmergeNewResourceList;
    }
}
