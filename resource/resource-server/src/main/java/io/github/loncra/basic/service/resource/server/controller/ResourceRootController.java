package io.github.loncra.basic.service.resource.server.controller;


import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.resource.server.service.enumerate.SystemResourceService;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.security.plugin.Plugin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 资源管理
 *
 * @author maurice.chen
 */
@Slf4j
@RefreshScope
@RestController
@RequiredArgsConstructor
public class ResourceRootController {

    private final SystemResourceService systemResourceService;

    /**
     * 获取服务枚举
     *
     * @param service       服务名
     * @param enumerateName 枚举名
     *
     * @return 枚举信息
     */
    @GetMapping("enumerate/{service}/{enumerateName}")
    public Object getServiceEnumerate(
            @PathVariable
            String service,
            @PathVariable
            String enumerateName,
            @RequestParam(required = false, defaultValue = "false")
            boolean idValueFormat,
            @RequestParam(required = false)
            List<String> ignoreValue
    ) {
        Map<String, Object> enumerate = systemResourceService.getServiceEnumerate(service, enumerateName, ignoreValue);
        return IdValueMetadata.ofMap(enumerate, idValueFormat);
    }

    /**
     * 批量获取服务枚举
     *
     * @param map key 为 service 值，value 为 enumerateName
     *
     * @return 服务枚举名称 为 key，对应的枚举集合为 value
     */
    @PostMapping("enumerate")
    public Map<String, Map<String, Object>> getServiceEnumerates(
            @RequestBody
            Map<String, List<IdValueMetadata<String, List<String>>>> map,
            @RequestParam(required = false, defaultValue = "false")
            boolean nameValueFormat
    ) {

        Map<String, Map<String, Object>> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<IdValueMetadata<String, List<String>>>> entry : map.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> valueMap = new LinkedHashMap<>();
            for (IdValueMetadata<String, List<String>> value : entry.getValue()) {
                Map<String, Object> enumerate = systemResourceService.getServiceEnumerate(key, value.getId(), value.getValue());
                if (nameValueFormat) {
                    List<Map<String, Object>> nameValueMap = new LinkedList<>();
                    enumerate.forEach((k,v) -> nameValueMap.add(Map.of(NameEnum.FIELD_NAME, k, ValueEnum.FIELD_NAME, v)));
                    valueMap.put(value.getId(), nameValueMap);
                }
                else {
                    valueMap.put(value.getId(), enumerate);
                }
            }
            result.put(key, valueMap);
        }

        return result;

    }

    /**
     * 同步所有枚举
     *
     * @return 所有服务枚举信息
     */
    @PostMapping("enumerate/sync")
    @PreAuthorize("hasAuthority('perms[resource_server_enumerate:sync]')")
    @ConditionalOnProperty(prefix = "loncra.basic-service.commons.app",value = "runtime-mode", havingValue = "MICROSERVICE")
    @Plugin(name = "同步所有枚举", parent = "enumerate", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE, audit = true)
    public RestResult<Map<String, Map<String, Map<String, Object>>>> syncEnumerate() throws Exception {
        systemResourceService.syncEnumerate();
        return RestResult.ofSuccess("同步系统枚举成功", systemResourceService.getServiceEnumerate());

    }

    /**
     * 获取服务枚举
     *
     * @return 服务枚举信息
     */
    @GetMapping("enumerate")
    @Plugin(name = "系统枚举查询", id = "enumerate", parent = "resource", type = ResourceTypeEnum.RESOURCE_MENU_TYPE, sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Map<String, Map<String, Map<String, Object>>> enumerate() {
        return systemResourceService.getServiceEnumerate();
    }

}
