package io.github.loncra.basic.service.ai.server.resolver.sandbox;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.harness.agent.sandbox.ExecResult;
import io.agentscope.harness.agent.sandbox.SandboxErrorCode;
import io.agentscope.harness.agent.sandbox.SandboxException;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.RpcFilesystemInvoker;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.RpcFilesystemScope;
import io.github.loncra.framework.commons.RestResult;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ClientSandbox extends AbstractCommandSandbox {

    public static final String COMMAND = "command";

    public static final String TIMEOUT_SECONDS = "timeoutSeconds";

    public static final String STDIN = "stdin";

    private final RpcFilesystemInvoker invoker;

    private final String deviceIdentified;

    private final RpcFilesystemScope scope;

    public ClientSandbox(RpcFilesystemInvoker invoker, String deviceIdentified, RpcFilesystemScope scope) {
        super(ClientSandboxClientOptions.VIRTUAL_ROOT);
        this.invoker = invoker;
        this.deviceIdentified = deviceIdentified;
        this.scope = scope;
    }

    @Override
    protected boolean probeWorkspaceRootForPreservedResume() {
        return true;
    }

    @Override
    protected ExecResult runCommand(RuntimeContext runtimeContext, String command, int timeoutSeconds, byte[] stdin) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(COMMAND, command);
        params.put(TIMEOUT_SECONDS, timeoutSeconds);
        if (stdin != null) {
            params.put(STDIN, new String(stdin, StandardCharsets.UTF_8));
        }
        RestResult<ExecResult> result = invoker.execute(deviceIdentified, scope, params);
        if (!result.isSuccess() || Objects.isNull(result.getData())) {
            throw new SandboxException(SandboxErrorCode.EXEC_NONZERO, result.getMessage());
        }
        return result.getData();
    }
}
