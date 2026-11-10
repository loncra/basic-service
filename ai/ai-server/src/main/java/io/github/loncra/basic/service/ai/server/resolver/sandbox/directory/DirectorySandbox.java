package io.github.loncra.basic.service.ai.server.resolver.sandbox.directory;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.harness.agent.sandbox.ExecResult;
import io.agentscope.harness.agent.sandbox.SandboxErrorCode;
import io.agentscope.harness.agent.sandbox.SandboxException;
import io.github.loncra.basic.service.ai.server.resolver.sandbox.AbstractCommandSandbox;
import org.apache.commons.lang3.SystemUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DirectorySandbox extends AbstractCommandSandbox {

    private final Path directory;

    public DirectorySandbox(Path directory) {
        super(directory.toAbsolutePath().normalize().toString());
        this.directory = directory.toAbsolutePath().normalize();
    }

    public Path directory() {
        return directory;
    }

    @Override
    protected void doSetupWorkspace() throws Exception {
        Files.createDirectories(directory);
    }

    @Override
    protected boolean probeWorkspaceRootForPreservedResume() {
        return Files.isDirectory(directory);
    }

    @Override
    protected ExecResult runCommand(RuntimeContext runtimeContext, String command, int timeoutSeconds, byte[] stdin) throws Exception {
        Files.createDirectories(directory);
        Process process = new ProcessBuilder(commandLine(command))
                .directory(directory.toFile())
                .start();
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        Thread outReader = Thread.startVirtualThread(() -> readInto(process.getInputStream(), stdout));
        Thread errReader = Thread.startVirtualThread(() -> readInto(process.getErrorStream(), stderr));
        try (OutputStream input = process.getOutputStream()) {
            if (stdin != null) {
                input.write(stdin);
            }
        }
        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new SandboxException(SandboxErrorCode.EXEC_TIMEOUT, "命令超过 " + timeoutSeconds + " 秒");
        }
        outReader.join();
        errReader.join();
        return new ExecResult(process.exitValue(), stdout.toString(), stderr.toString(), false);
    }

    private static List<String> commandLine(String command) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return List.of("powershell", "-NoProfile", "-NonInteractive", "-Command", command);
        }
        return List.of("bash", "-lc", command);
    }

    private static void readInto(InputStream input, StringBuilder target) {
        try {
            target.append(new String(input.readAllBytes(), StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }
}
