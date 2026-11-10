package io.github.loncra.basic.service.ai.server.resolver.sandbox;

import io.agentscope.harness.agent.sandbox.Sandbox;
import io.agentscope.harness.agent.sandbox.SandboxClient;
import io.agentscope.harness.agent.sandbox.SandboxState;
import io.agentscope.harness.agent.sandbox.WorkspaceSpec;
import io.agentscope.harness.agent.sandbox.snapshot.SandboxSnapshotSpec;

public class ClientSandboxClient implements SandboxClient<ClientSandboxClientOptions> {

    private final ClientSandboxClientOptions options;

    public ClientSandboxClient(ClientSandboxClientOptions options) {
        this.options = options;
    }

    @Override
    public Sandbox create(WorkspaceSpec workspaceSpec, SandboxSnapshotSpec snapshotSpec, ClientSandboxClientOptions options) {
        return sandbox(options);
    }

    @Override
    public Sandbox resume(SandboxState state) {
        return sandbox(options);
    }

    @Override
    public void delete(Sandbox sandbox) {
    }

    @Override
    public String serializeState(SandboxState state) {
        return ClientSandboxClientOptions.VIRTUAL_ROOT;
    }

    @Override
    public SandboxState deserializeState(String json) {
        return CommandSandboxState.of(ClientSandboxClientOptions.VIRTUAL_ROOT);
    }

    private Sandbox sandbox(ClientSandboxClientOptions options) {
        ClientSandboxClientOptions current = options != null ? options : this.options;
        return new ClientSandbox(current.invoker(), current.deviceIdentified(), current.scope());
    }
}
