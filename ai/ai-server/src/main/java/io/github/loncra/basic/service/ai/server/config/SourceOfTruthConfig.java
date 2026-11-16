package io.github.loncra.basic.service.ai.server.config;

import io.agentscope.harness.agent.filesystem.local.LocalFilesystemWithShell;
import io.github.loncra.basic.service.ai.server.enumerate.agent.SourceOfTruthTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@NoArgsConstructor
@EqualsAndHashCode
@ConfigurationProperties("loncra.basic-service.ai.app.source-of-truth")
public class SourceOfTruthConfig {
    private SourceOfTruthTypeEnum type = SourceOfTruthTypeEnum.LOCAL;
    private String path = "./ai-source-of-truth";
    private String dataDir = ".loncra-admin";
    private int executeTimeoutSeconds = LocalFilesystemWithShell.DEFAULT_EXECUTE_TIMEOUT;
    private int maxOutputBytes = 100_000;
}
