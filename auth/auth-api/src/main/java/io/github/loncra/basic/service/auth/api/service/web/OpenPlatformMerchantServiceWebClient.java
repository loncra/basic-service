package io.github.loncra.basic.service.auth.api.service.web;

import io.github.loncra.basic.service.auth.api.service.OpenPlatformMerchantServiceClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;


@HttpExchange("open/platform")
public interface OpenPlatformMerchantServiceWebClient extends OpenPlatformMerchantServiceClient {

    @Override
    @GetExchange("merchant/client/findByClientId")
    RegisteredClient findByClientId(@RequestParam String clientId);

    @Override
    @GetExchange("merchant/client/findById")
    RegisteredClient findById(@RequestParam String id);
}
