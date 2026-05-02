package io.github.loncra.basic.service.resource.server.service.enumerate.scan;

import io.github.loncra.basic.service.resource.server.service.enumerate.AbstractRedissonSystemResourceService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.spring.web.config.SpringWebMvcProperties;
import io.github.loncra.framework.spring.web.endpoint.EnumerateEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/***
 * 扫描模块形式的枚举资源服务实现
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScanModuleSystemResourceService extends AbstractRedissonSystemResourceService implements InitializingBean {

    /**
     * 信息奉献者集合
     */
    private final List<InfoContributor> infoContributors;

    private final SpringWebMvcProperties properties;

    @Override
    public void syncEnumerate() throws Exception {
        throw new UnsupportedOperationException("单体服务不支持");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (Map.Entry<String, List<String>> entry : getResourceAppConfig().getScanPluginPackages().entrySet()) {
            if (log.isDebugEnabled()) {
                log.debug("开始绑定 [{}] 应用枚举资源信息", entry.getKey());
            }
            SpringWebMvcProperties moduleProperties = CastUtils.of(properties, SpringWebMvcProperties.class);
            moduleProperties.setEnumerateEndpointBasePackages(entry.getValue());
            EnumerateEndpoint enumerateEndpoint = new EnumerateEndpoint(infoContributors, moduleProperties);
            Map<String, Object> enumerate = enumerateEndpoint.enumerateEndpoint();

            if (MapUtils.isEmpty(enumerate)) {
                return;
            }

            if (!enumerate.containsKey(EnumerateEndpoint.DEFAULT_ENUM_KEY_NAME)) {
                return;
            }

            Map<String, Map<String, Object>> enumerateData = CastUtils.cast(enumerate.get(EnumerateEndpoint.DEFAULT_ENUM_KEY_NAME));

            getServiceEnumerate().put(entry.getKey(), enumerateData);
        }

    }
}
