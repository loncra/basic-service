package io.github.loncra.basic.service.ai.server.resolver.sandbox;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.harness.agent.sandbox.AbstractBaseSandbox;
import io.agentscope.harness.agent.sandbox.ExecResult;

import java.io.InputStream;

public abstract class AbstractCommandSandbox extends AbstractBaseSandbox {

    protected AbstractCommandSandbox(String root) {
        super(CommandSandboxState.of(root));
    }

    @Override
    protected String getWorkspaceRoot() {
        return getState().getWorkspaceSpec().getRoot();
    }

    @Override
    protected ExecResult doExec(RuntimeContext runtimeContext, String command, int timeoutSeconds) throws Exception {
        ExecResult result = runCommand(runtimeContext, command, timeoutSeconds, null);
        return new ExecResult(result.exitCode(), result.stdout(), "", result.truncated());
    }

    public ExecResult execWithInput(RuntimeContext runtimeContext, String command, int timeoutSeconds, byte[] stdin) throws Exception {
        return runCommand(runtimeContext, command, timeoutSeconds, stdin);
    }

    protected abstract ExecResult runCommand(
            RuntimeContext runtimeContext,
            String command,
            int timeoutSeconds,
            byte[] stdin
    ) throws Exception;

    @Override
    protected void doSetupWorkspace() throws Exception {
    }

    @Override
    protected void doDestroyWorkspace() throws Exception {
    }

    @Override
    protected InputStream doPersistWorkspace() throws Exception {
        throw new UnsupportedOperationException("本轮不做沙箱快照");
    }

    @Override
    protected void doHydrateWorkspace(InputStream archive) throws Exception {
        throw new UnsupportedOperationException("本轮不做沙箱快照");
    }
}
