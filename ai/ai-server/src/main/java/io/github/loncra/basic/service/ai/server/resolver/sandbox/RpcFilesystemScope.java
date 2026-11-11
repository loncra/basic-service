package io.github.loncra.basic.service.ai.server.resolver.sandbox;

import io.github.loncra.basic.service.ai.server.resolver.channel.AbstractAgentChannelResolver;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import org.springframework.core.env.AbstractEnvironment;

import java.util.Map;

public sealed interface RpcFilesystemScope {

    String VIRTUAL_ROOT = "workspace";

    String STORE_VALUE = "store";

    String STORE_BASE_FIELD = "base";

    Map<String, Object> toPayload();

    record Store(String base) implements RpcFilesystemScope {

        @Override
        public Map<String, Object> toPayload() {
            return Map.of(TypeIdNameMetadata.TYPE_FIELD_NAME, STORE_VALUE, STORE_BASE_FIELD, base);
        }
    }

    record DefaultWorkspace() implements RpcFilesystemScope {

        @Override
        public Map<String, Object> toPayload() {
            return Map.of(TypeIdNameMetadata.TYPE_FIELD_NAME, VIRTUAL_ROOT, AbstractEnvironment.RESERVED_DEFAULT_PROFILE_NAME, true);
        }
    }

    record CustomWorkspace(String key) implements RpcFilesystemScope {

        @Override
        public Map<String, Object> toPayload() {
            return Map.of(TypeIdNameMetadata.TYPE_FIELD_NAME, VIRTUAL_ROOT, AbstractAgentChannelResolver.WORKSPACE_KEY_METADATA_KEY, key);
        }
    }
}
