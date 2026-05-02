package io.github.loncra.basic.service.resource.server.service.enumerate;

import java.util.List;
import java.util.Map;

/**
 * 枚举资源服务接口，用于获取当前系统的所有枚举使用
 *
 * @author maurice.chen
 */
public interface SystemResourceService {
    /**
     * 获取服务枚举内容
     *
     * @return 服务枚举内容
     */
    Map<String, Map<String, Map<String, Object>>> getServiceEnumerate();

    /**
     * 同步枚举
     */
    void syncEnumerate()  throws Exception;

    /**
     * 获取服务枚举
     *
     * @param key 服务名称
     * @param id 枚举 id
     * @param value 枚举值
     *
     * @return 枚举内容
     */
    Map<String, Object> getServiceEnumerate(
            String key,
            String id,
            List<String> value
    );

}
