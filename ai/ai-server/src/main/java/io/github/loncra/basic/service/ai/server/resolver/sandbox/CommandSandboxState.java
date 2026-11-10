package io.github.loncra.basic.service.ai.server.resolver.sandbox;

import io.agentscope.harness.agent.sandbox.SandboxState;
import io.agentscope.harness.agent.sandbox.WorkspaceSpec;

public class CommandSandboxState extends SandboxState {

    public static CommandSandboxState of(String root) {
        CommandSandboxState state = new CommandSandboxState();
        WorkspaceSpec spec = new WorkspaceSpec();
        spec.setRoot(root);
        state.setWorkspaceSpec(spec);
        return state;
    }
}
