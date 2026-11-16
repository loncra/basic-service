package io.github.loncra.basic.service.ai.server.resolver.filesystem;

import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.local.LocalFilesystemWithShell;
import io.github.loncra.basic.service.ai.server.config.SourceOfTruthConfig;
import io.github.loncra.basic.service.ai.server.enumerate.agent.SourceOfTruthTypeEnum;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class LocalFilesystemSourceOfTruthConstructor implements SourceOfTruthFileSystemConstructor {

    @Override
    public SourceOfTruthTypeEnum getType() {
        return SourceOfTruthTypeEnum.LOCAL;
    }

    @Override
    public AbstractFilesystem getSourceOfTruthFileSystem(
            SourceOfTruthConfig sourceOfTruthConfig,
            String relativePath
    ) {
        Path dataRoot = Path.of(sourceOfTruthConfig.getPath()).toAbsolutePath().normalize();
        return new LocalFilesystemWithShell(
                relativePath.isBlank() ? dataRoot : dataRoot.resolve(relativePath).normalize(),
                true,
                sourceOfTruthConfig.getExecuteTimeoutSeconds(),
                sourceOfTruthConfig.getMaxOutputBytes(),
                null,
                false,
                null
        );
    }
}
