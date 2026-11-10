package io.github.loncra.basic.service.ai.server.resolver.sandbox.directory;

import io.agentscope.harness.agent.sandbox.Sandbox;
import io.agentscope.harness.agent.sandbox.SandboxClient;
import io.agentscope.harness.agent.sandbox.SandboxState;
import io.agentscope.harness.agent.sandbox.WorkspaceSpec;
import io.agentscope.harness.agent.sandbox.snapshot.SandboxSnapshotSpec;
import io.github.loncra.basic.service.ai.server.resolver.sandbox.CommandSandboxState;

import java.nio.file.Path;

public class DirectorySandboxClient implements SandboxClient<DirectorySandboxClientOptions> {

    @Override
    public Sandbox create(WorkspaceSpec workspaceSpec, SandboxSnapshotSpec snapshotSpec, DirectorySandboxClientOptions options) {
        return new DirectorySandbox(Path.of(root(workspaceSpec, options)));
    }

    @Override
    public Sandbox resume(SandboxState state) {
        return new DirectorySandbox(Path.of(state.getWorkspaceSpec().getRoot()));
    }

    @Override
    public void delete(Sandbox sandbox) {
    }

    @Override
    public String serializeState(SandboxState state) {
        return state.getWorkspaceSpec().getRoot();
    }

    @Override
    public SandboxState deserializeState(String json) {
        return CommandSandboxState.of(json);
    }

    private static String root(WorkspaceSpec workspaceSpec, DirectorySandboxClientOptions options) {
        if (workspaceSpec != null && workspaceSpec.getRoot() != null) {
            return workspaceSpec.getRoot();
        }
        return options.getWorkspaceRoot();
    }
}
