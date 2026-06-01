package io.github.loncra.basic.service.auth.api.service;

import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;


public interface OpenPlatformMerchantServiceClient extends RegisteredClientRepository {

    @Override
    default void save(RegisteredClient registeredClient) {
        throw new UnsupportedOperationException("unsupported save(RegisteredClient registeredClient)");
    }

    @Override
    @Nullable RegisteredClient findByClientId(String clientId);

    @Override
    @Nullable RegisteredClient findById(String id);
}
