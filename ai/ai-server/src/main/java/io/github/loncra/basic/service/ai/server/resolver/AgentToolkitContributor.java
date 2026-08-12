package io.github.loncra.basic.service.ai.server.resolver;


import io.agentscope.core.tool.Toolkit;

/**
 * agent 工具奉献者,用于针对需要再 agent 执行前动态根据条件加入工具使用
 *
 * @author maurice.chen
 */
public interface AgentToolkitContributor {

    /**
     * 贡献工具
     *
     * @param toolkit 工具包
     */
    void contribute(Toolkit toolkit);

}
