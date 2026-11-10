package io.github.loncra.basic.service.ai.server.config;

import io.agentscope.harness.agent.sandbox.WorkspaceSpec;
import io.agentscope.harness.agent.sandbox.impl.docker.DockerFilesystemSpec;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 对齐 {@code DockerSandboxClientOptions}。仅 {@code sandbox-type=docker} 时使用。
 */
@Data
@NoArgsConstructor
public class DockerSandboxConfig {

    /** Docker 镜像。 */
    private String image = "ubuntu:22.04";

    /** 容器内工作区根路径。 */
    private String workspaceRoot = "/workspace";

    /**
     * 注入容器的环境变量。
     * 官方 {@code DockerSandboxClient.create} 不读取 options 上的 environment，
     * {@code docker run -e} 来自 {@link WorkspaceSpec}。
     */
    private Map<String, String> environment = new LinkedHashMap<>();

    /** 内存上限，字节。为空不限制。 */
    private Long memorySizeBytes;

    /** CPU 个数。为空不限制。 */
    private Long cpuCount;

    /** 暴露端口。官方按相同端口映射，即 {@code -p port:port}。 */
    private int[] exposedPorts = {};

    /** 追加到 {@code docker run}、镜像名之前的参数。 */
    private List<String> additionalRunArgs = new ArrayList<>();

    /** {@code docker run --network}。为空时官方使用 {@code none}。 */
    private String network;

    public DockerFilesystemSpec toSpec() {
        DockerFilesystemSpec spec = new DockerFilesystemSpec()
                .image(image)
                .workspaceRoot(workspaceRoot)
                .environment(environment)
                .memorySizeBytes(memorySizeBytes)
                .cpuCount(cpuCount)
                .network(network)
                .additionalRunArgs(additionalRunArgs);
        if (exposedPorts != null && exposedPorts.length > 0) {
            spec.exposedPorts(exposedPorts);
        }
        if (environment != null && !environment.isEmpty()) {
            WorkspaceSpec workspaceSpec = new WorkspaceSpec();
            workspaceSpec.setRoot(workspaceRoot);
            workspaceSpec.setEnvironment(environment);
            spec.workspaceSpec(workspaceSpec);
        }
        return spec;
    }
}
