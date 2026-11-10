package io.github.loncra.basic.service.ai.server.resolver.filesystem;

import io.agentscope.harness.agent.sandbox.ExecResult;
import io.github.loncra.framework.commons.RestResult;

import java.util.Map;

public interface RpcFilesystemInvoker {

    RestResult<ExecResult> execute(
            String deviceIdentified,
            RpcFilesystemScope scope,
            Map<String, Object> params
    );

}
