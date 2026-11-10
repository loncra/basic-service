package io.github.loncra.basic.service.ai.server.resolver.sandbox;

import io.agentscope.harness.agent.sandbox.SandboxClient;
import io.agentscope.harness.agent.sandbox.SandboxClientOptions;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.RpcFilesystemInvoker;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.RpcFilesystemScope;

public class ClientSandboxClientOptions extends SandboxClientOptions {

    public static final String TYPE = "client";

    public static final String VIRTUAL_ROOT = "/workspace";

    private final RpcFilesystemInvoker invoker;

    private final String deviceIdentified;

    private final RpcFilesystemScope scope;

    public ClientSandboxClientOptions(
            RpcFilesystemInvoker invoker,
            String deviceIdentified,
            RpcFilesystemScope scope
    ) {
        this.invoker = invoker;
        this.deviceIdentified = deviceIdentified;
        this.scope = scope;
    }

    public RpcFilesystemInvoker invoker() {
        return invoker;
    }

    public String deviceIdentified() {
        return deviceIdentified;
    }

    public RpcFilesystemScope scope() {
        return scope;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getWorkspaceRoot() {
        return VIRTUAL_ROOT;
    }

    @Override
    public SandboxClient<? extends SandboxClientOptions> createClient() {
        return new ClientSandboxClient(this);
    }
}
