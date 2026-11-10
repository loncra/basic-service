package io.github.loncra.basic.service.ai.server.resolver.sandbox.directory;

import io.agentscope.harness.agent.sandbox.SandboxClient;
import io.agentscope.harness.agent.sandbox.SandboxClientOptions;

public class DirectorySandboxClientOptions extends SandboxClientOptions {

    public static final String TYPE = "directory";

    private String root;

    public DirectorySandboxClientOptions root(String root) {
        this.root = root;
        return this;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getWorkspaceRoot() {
        return root;
    }

    @Override
    public SandboxClient<? extends SandboxClientOptions> createClient() {
        return new DirectorySandboxClient();
    }
}
