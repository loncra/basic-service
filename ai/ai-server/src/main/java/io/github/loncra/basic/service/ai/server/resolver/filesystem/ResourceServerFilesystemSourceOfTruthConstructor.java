package io.github.loncra.basic.service.ai.server.resolver.filesystem;

import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.remote.RemoteFilesystem;
import io.agentscope.harness.agent.filesystem.remote.store.BaseStore;
import io.github.loncra.basic.service.ai.server.config.SourceOfTruthConfig;
import io.github.loncra.basic.service.ai.server.enumerate.agent.SourceOfTruthTypeEnum;
import io.github.loncra.basic.service.resource.api.enumerate.AttachmentTypeEnum;
import io.github.loncra.basic.service.resource.api.service.AttachmentServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ResourceServerFilesystemSourceOfTruthConstructor implements SourceOfTruthFileSystemConstructor{

    private final AttachmentServiceClient attachmentServiceClient;

    @Override
    public SourceOfTruthTypeEnum getType() {
        return SourceOfTruthTypeEnum.RESOURCE_SERVER;
    }

    @Override
    public AbstractFilesystem getSourceOfTruthFileSystem(
            SourceOfTruthConfig sourceOfTruthConfig,
            String relativePath
    ) {
        BaseStore store = new ResourceServerBaseStore(
                attachmentServiceClient,
                AttachmentTypeEnum.SYSTEM_FILE.getValue(),
                sourceOfTruthConfig.getPath()
        );
        List<String> namespace = relativePath.isBlank()
                ? List.of(sourceOfTruthConfig.getDataDir())
                : List.of(sourceOfTruthConfig.getDataDir(), relativePath);
        return new RemoteFilesystem(store, namespace);
    }
}
