package io.github.loncra.basic.service.ai.server.resolver.filesystem;

import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.github.loncra.basic.service.ai.server.config.SourceOfTruthConfig;
import io.github.loncra.basic.service.ai.server.enumerate.agent.SourceOfTruthTypeEnum;

public interface SourceOfTruthFileSystemConstructor {

    SourceOfTruthTypeEnum getType();

    AbstractFilesystem getSourceOfTruthFileSystem(SourceOfTruthConfig sourceOfTruthConfig, String relativePath);
}
