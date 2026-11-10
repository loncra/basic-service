package io.github.loncra.basic.service.ai.server.resolver.sandbox.directory;

import io.agentscope.harness.agent.filesystem.spec.SandboxFilesystemSpec;
import io.agentscope.harness.agent.sandbox.SandboxClient;
import io.agentscope.harness.agent.sandbox.SandboxClientOptions;
import io.agentscope.harness.agent.sandbox.WorkspaceSpec;
import io.agentscope.harness.agent.sandbox.snapshot.NoopSnapshotSpec;
import io.agentscope.harness.agent.sandbox.snapshot.SandboxSnapshotSpec;

import java.nio.file.Path;

public class DirectoryFilesystemSpec extends SandboxFilesystemSpec {

    private final WorkspaceSpec workspaceSpec;

    private final DirectorySandboxClientOptions options;

    private DirectoryFilesystemSpec(Path directory) {
        Path root = directory.toAbsolutePath().normalize();
        workspaceSpec = new WorkspaceSpec();
        workspaceSpec.setRoot(root.toString());
        options = new DirectorySandboxClientOptions().root(root.toString());
        workspaceProjectionEnabled(false);
    }

    public static DirectoryFilesystemSpec forDirectory(Path directory) {
        return new DirectoryFilesystemSpec(directory);
    }

    @Override
    protected SandboxClient<?> createClient() {
        return options.createClient();
    }

    @Override
    protected SandboxClientOptions clientOptions() {
        return options;
    }

    @Override
    protected SandboxSnapshotSpec snapshotSpec() {
        return new NoopSnapshotSpec();
    }

    @Override
    protected WorkspaceSpec workspaceSpec() {
        return workspaceSpec;
    }
}
