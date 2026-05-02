package io.github.loncra.basic.service.auth.api.service.feign;

import io.github.loncra.basic.service.auth.api.service.OpenPlatformMerchantServiceClient;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.framework.spring.security.core.authentication.service.feign.FeignAuthenticationConfiguration;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@ConditionalOnClass(FeignClientsConfiguration.class)
@FeignClient(name = SystemConstants.SYS_AUTH_NAME, contextId = "openPlatformMerchantServiceFeignClient", configuration = FeignAuthenticationConfiguration.class)
public interface OpenPlatformMerchantServiceFeignClient extends OpenPlatformMerchantServiceClient {

    @Override
    @GetMapping("open/platform/merchant/client/findByClientId")
    @Nullable RegisteredClient findByClientId(@RequestParam String clientId);

    @Override
    @GetMapping("open/platform/merchant/client/findById")
    @Nullable RegisteredClient findById(@RequestParam String id);
}
