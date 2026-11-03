package io.github.loncra.basic.service.ai.server.resolver.filesystem;

import io.github.loncra.basic.service.ai.server.enumerate.RpcFilesystemOperationEnum;
import io.github.loncra.framework.commons.RestResult;

import java.util.Map;

public interface RpcFilesystemInvoker {

    RestResult<Object> invoke(
            String deviceIdentified,
            RpcFilesystemOperationEnum operation,
            Map<String, Object> params
    );

    Map<String, Object> getClientEnvironment(
            String principal,
            String deviceIdentified
    );

}
