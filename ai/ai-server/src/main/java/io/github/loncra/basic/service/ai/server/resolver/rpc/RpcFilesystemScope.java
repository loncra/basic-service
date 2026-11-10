package io.github.loncra.basic.service.ai.server.resolver.rpc;

import java.util.Map;

public sealed interface RpcFilesystemScope {

    Map<String, Object> toPayload();

    record Store() implements RpcFilesystemScope {

        @Override
        public Map<String, Object> toPayload() {
            return Map.of("type", "store");
        }
    }

    record DefaultWorkspace() implements RpcFilesystemScope {

        @Override
        public Map<String, Object> toPayload() {
            return Map.of("type", "workspace", "default", true);
        }
    }

    record CustomWorkspace(String key) implements RpcFilesystemScope {

        @Override
        public Map<String, Object> toPayload() {
            return Map.of("type", "workspace", "key", key);
        }
    }
}
