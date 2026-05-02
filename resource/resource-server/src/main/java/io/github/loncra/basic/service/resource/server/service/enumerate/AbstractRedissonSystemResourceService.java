package io.github.loncra.basic.service.resource.server.service.enumerate;

import com.alibaba.nacos.api.naming.pojo.Instance;
import io.github.loncra.basic.service.resource.server.config.ResourceAppConfig;
import io.github.loncra.framework.commons.CastUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 抽象的 redis 系统资源服务实现
 *
 * @author maurice.chen
 */
@Slf4j
@Getter
@Setter(onMethod_ =  @Autowired)
public abstract class AbstractRedissonSystemResourceService implements SystemResourceService {


    private static final Map<String, Instance> INSTANCE_CACHE = new LinkedHashMap<>();

    private RedissonClient redissonClient;

    private ResourceAppConfig resourceAppConfig;

    /**
     * 获取服务枚举
     *
     * @return 枚举名称集合
     */
    @Override
    public Map<String, Map<String, Map<String, Object>>> getServiceEnumerate() {
        return getRedissonClient().getMap(getResourceAppConfig().getEnumerateCache().getName());
    }

    /**
     * 获取枚举信息
     *
     * @param service  服务名称
     * @param enumName 枚举名称
     *
     * @return 枚举信息
     */
    @Override
    public Map<String, Object> getServiceEnumerate(
            String service,
            String enumName,
            List<String> ignoreValue
    ) {
        Map<String, Object> result = new LinkedHashMap<>();

        Map<String, Map<String, Map<String, Object>>> data = getServiceEnumerate();

        if (!data.containsKey(service)) {
            return result;
        }

        Map<String, Map<String, Object>> enumMap = data.get(service);

        if (!enumMap.containsKey(enumName)) {
            return result;
        }

        Map<String, Object> enumData = enumMap.get(enumName);

        if (CollectionUtils.isEmpty(ignoreValue)) {
            ignoreValue = new LinkedList<>();
        }

        for (Map.Entry<String, Object> entry : enumData.entrySet()) {
            if (ignoreValue.contains(entry.getKey()) || ignoreValue.contains(entry.getValue().toString())) {
                continue;
            }

            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * 计算对象哈希，用于内容变更检测
     */
    protected String buildHash(Object target) {
        try {
            byte[] bytes = CastUtils.getObjectMapper()
                    .writeValueAsBytes(target);
            return DigestUtils.md5DigestAsHex(bytes);
        }
        catch (Exception e) {
            log.warn("计算枚举内容哈希失败，将视为内容变化: {}", e.getMessage());
            return DigestUtils.md5DigestAsHex(Objects.toString(UUID.randomUUID()).getBytes(StandardCharsets.UTF_8));
        }
    }

}
