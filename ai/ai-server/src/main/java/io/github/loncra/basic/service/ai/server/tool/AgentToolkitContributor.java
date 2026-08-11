package io.github.loncra.basic.service.ai.server.tool;


import io.agentscope.core.tool.Toolkit;

public interface AgentToolkitContributor {
    void contribute(Toolkit toolkit);
}
