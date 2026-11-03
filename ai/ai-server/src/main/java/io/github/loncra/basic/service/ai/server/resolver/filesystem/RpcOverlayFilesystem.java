package io.github.loncra.basic.service.ai.server.resolver.filesystem;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.OverlayFilesystem;
import io.agentscope.harness.agent.filesystem.model.ExecuteResponse;
import io.agentscope.harness.agent.filesystem.sandbox.AbstractSandboxFilesystem;

public class RpcOverlayFilesystem extends OverlayFilesystem implements AbstractSandboxFilesystem {

    private final AbstractSandboxFilesystem sandbox;

    public RpcOverlayFilesystem(
            AbstractSandboxFilesystem upper,
            AbstractFilesystem lower
    ) {
        super(upper, lower);
        this.sandbox = upper;
    }

    @Override
    public String id() {
        return sandbox.id();
    }

    @Override
    public ExecuteResponse execute(
            RuntimeContext runtimeContext,
            String command,
            Integer timeoutSeconds
    ) {
        return sandbox.execute(runtimeContext, command, timeoutSeconds);
    }

}
