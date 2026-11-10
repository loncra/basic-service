package io.github.loncra.basic.service.ai.server.resolver.sandbox;

import io.agentscope.harness.agent.filesystem.spec.SandboxFilesystemSpec;
import io.agentscope.harness.agent.sandbox.SandboxClient;
import io.agentscope.harness.agent.sandbox.SandboxClientOptions;
import io.agentscope.harness.agent.sandbox.WorkspaceSpec;
import io.agentscope.harness.agent.sandbox.snapshot.NoopSnapshotSpec;
import io.agentscope.harness.agent.sandbox.snapshot.SandboxSnapshotSpec;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.RpcFilesystemInvoker;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.RpcFilesystemScope;

public class ClientFilesystemSpec extends SandboxFilesystemSpec {

    private final ClientSandboxClientOptions options;

    private final WorkspaceSpec workspaceSpec;

    private ClientFilesystemSpec(RpcFilesystemInvoker invoker, String deviceIdentified, RpcFilesystemScope scope) {
        options = new ClientSandboxClientOptions(invoker, deviceIdentified, scope);
        workspaceSpec = new WorkspaceSpec();
        workspaceSpec.setRoot(ClientSandboxClientOptions.VIRTUAL_ROOT);
        workspaceProjectionEnabled(false);
    }

    public static ClientFilesystemSpec forDevice(
            RpcFilesystemInvoker invoker,
            String deviceIdentified,
            RpcFilesystemScope scope
    ) {
        return new ClientFilesystemSpec(invoker, deviceIdentified, scope);
    }

    @Override
    protected SandboxClient<?> createClient() {
        return options.createClient();
    }

    @Override
    protected SandboxClientOptions clientOptions() {
        return options;
    }

    @Override
    protected SandboxSnapshotSpec snapshotSpec() {
        return new NoopSnapshotSpec();
    }

    @Override
    protected WorkspaceSpec workspaceSpec() {
        return workspaceSpec;
    }
}
