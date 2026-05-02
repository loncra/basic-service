package io.github.loncra.basic.service.auth.server.resolver;

import io.github.loncra.basic.service.auth.server.domain.dto.AbstractSyncPluginResourceDto;
import io.github.loncra.basic.service.auth.server.domain.dto.DisabledApplicationResourceDto;

/**
 * 插件资源拦截器
 *
 * @author maurice.chen
 */
public interface PluginResourceResolver {

    /**
     * 同步插件完成后的处理
     *
     * @param dto 同步插件资源实例信息
     */
    void postSyncPlugin(AbstractSyncPluginResourceDto dto);

    /**
     * 禁用应用资源后的处理
     *
     * @param dto 禁用应用资源信息
     */
    default void postDisabledApplicationResource(DisabledApplicationResourceDto dto) {

    }
}
