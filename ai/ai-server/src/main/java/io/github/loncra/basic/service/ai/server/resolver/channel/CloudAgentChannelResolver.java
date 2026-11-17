package io.github.loncra.basic.service.ai.server.resolver.channel;

import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.IsolationScope;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.RoutedSandboxFilesystem;
import io.agentscope.harness.agent.filesystem.sandbox.AbstractSandboxFilesystem;
import io.agentscope.harness.agent.filesystem.spec.LocalFilesystemSpec;
import io.agentscope.harness.agent.skill.runtime.ShellPathPolicy;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatBasicResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentResumeRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecuteMessageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.SandboxTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.SourceOfTruthFileSystemFactory;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemProperties;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CloudAgentChannelResolver extends AbstractAgentChannelResolver {

    @Override
    public ExecutionChannelEnum getChannel() {
        return ExecutionChannelEnum.CLOUD;
    }

    @Override
    public AgentChatBasicResponseBody resume(
            AgentResumeRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        AgentChatBasicResponseBody responseBody = super.resume(body, token, deviceIdentified);
        agentSseStreamPublishResolver.sendExecuteMessage(
                AgentExecuteMessageMetadata.of(getChannel(), responseBody.getAssistantMessageId(), deviceIdentified),
                false
        );
        return responseBody;
    }

    @Override
    protected HarnessAgent definitionHarnessAgent(
            HarnessAgent.Builder builder,
            AgentExecutionMetadata execution
    ) {
        if (SandboxTypeEnum.DOCKER.equals(aiAppConfig.getWorkspace().getSandboxType())) {
            userLevelRouteMap(dir -> sourceOfTruthFileSystemFactory.getUserDataDirFilesystem(execution.getAssistantMessage().getPrincipal(), dir))
                    .forEach(builder::filesystemRoute);

            String root = StringUtils.defaultIfBlank(aiAppConfig.getDocker().getWorkspaceRoot(), ShellPathPolicy.SANDBOX_WORKSPACE_PREFIX);

            builder.filesystem(aiAppConfig.getDocker().toSpec())
                    .environmentMemory("云端环境：Docker 容器，执行命令的终端为 bash，工作区根为 " + root + "。执行命令时请使用 bash 语法。");

        } else {
            AgentConversationEntity workspace = Objects.requireNonNull(
                    getAgentRecordStore().getConversation(execution.getToken().getName(), execution.getConversation().getParentId(), execution.getDeviceIdentified()),
                    "找不到 ID 为 [" + execution.getConversation().getParentId() + "] 的工作空间"
            );

            String workspacePath = resolveWorkspacePath(workspace);
            Path directory = Path.of(workspacePath);
            builder.abstractFilesystem(new RoutedSandboxFilesystem(
                    primaryFilesystem(directory, directory),
                    userLevelRouteMap(dir -> sourceOfTruthFileSystemFactory.getUserDataDirFilesystem(execution.getAssistantMessage().getPrincipal(), dir))
            ));

            String shell = SystemUtils.IS_OS_WINDOWS ? "cmd.exe /c" : "sh -c";
            builder.environmentMemory("云端环境：操作系统 " + SystemProperties.getOsName() + "，执行命令的终端为 " + shell + "，工作区是服务器本机目录。文件工具不经过终端。执行命令时请使用与 " + shell + " 匹配的语法。");
        }

        return builder.build();
    }

    private AbstractSandboxFilesystem primaryFilesystem(Path workspace, Path project) {
        AbstractFilesystem filesystem = new LocalFilesystemSpec()
                .project(project)
                .toFilesystem(workspace, IsolationScope.USER.toNamespaceFactory());
        if (filesystem instanceof AbstractSandboxFilesystem sandbox) {
            return sandbox;
        }
        throw new SystemException("本地文件系统规格未产出沙箱文件系统: " + filesystem.getClass().getName());
    }

    private String cloudWorkspaceDir(AgentConversationEntity workspace) {
        return SourceOfTruthFileSystemFactory.workspaceDirectory(
                Path.of(aiAppConfig.getWorkspace().getPath()), workspace.getId()).toString();
    }

    private String resolveWorkspacePath(AgentConversationEntity workspace) {
        Object roots = workspace.getMetadata().get(WORKSPACE_ROOTS_METADATA_KEY);
        if (roots instanceof List<?> list && !list.isEmpty()) {
            return String.valueOf(list.getFirst());
        }
        return cloudWorkspaceDir(workspace);
    }

    @Override
    protected void preCreateWorkspace(
            AgentConversationEntity workspace,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        if (workspace.getMetadata().containsKey(WORKSPACE_ROOTS_METADATA_KEY)) {
            return;
        }
        workspace.getMetadata().put(WORKSPACE_ROOTS_METADATA_KEY, List.of(cloudWorkspaceDir(workspace)));
    }

    @Override
    protected AbstractFilesystem openRecords(String deviceIdentified) {
        return sourceOfTruthFileSystemFactory.getSourceOfTruthFileSystem(StringUtils.EMPTY);
    }

}
