package io.github.loncra.basic.service.auth.api.service;

import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.bind.annotation.RequestParam;


public interface OpenPlatformMerchantServiceClient extends RegisteredClientRepository {

    @Override
    default void save(RegisteredClient registeredClient) {
        throw new UnsupportedOperationException("unsupported save(RegisteredClient registeredClient)");
    }

    @Override
    @Nullable RegisteredClient findByClientId(@RequestParam String clientId);

    @Override
    @Nullable RegisteredClient findById(@RequestParam String id);
}
