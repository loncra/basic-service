package io.github.loncra.basic.service.ai.server.resolver.channel;

import io.agentscope.core.skill.repository.FileSystemSkillRepository;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatBasicResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentResumeRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecuteMessageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.BasicAgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.SandboxTypeEnum;
import io.agentscope.harness.agent.filesystem.spec.LocalFilesystemSpec;
import io.github.loncra.basic.service.ai.server.resolver.sandbox.AgentSandboxLayout;
import io.github.loncra.basic.service.ai.server.resolver.sandbox.SandboxRecords;
import io.github.loncra.basic.service.ai.server.resolver.sandbox.SandboxFileAgentStateStore;
import io.github.loncra.basic.service.ai.server.service.agent.AgentHubSkillService;
import io.github.loncra.basic.service.ai.server.service.agent.AgentUserPluginToolkitService;
import io.github.loncra.basic.service.ai.server.service.agent.HubSkillRepository;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CloudAgentChannelResolver extends AbstractAgentChannelResolver {

    private final AgentUserPluginToolkitService agentUserPluginToolkitService;

    private final AgentHubSkillService agentHubSkillService;

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
                true
        );
        return responseBody;
    }

    @Override
    public HarnessAgent createHarnessAgent(AgentExecutionMetadata execution) {
        AgentMessageEntity assistant = execution.getAssistantMessage();
        AgentConversationEntity conversation = Objects.requireNonNull(
                execution.getConversation(),
                "找不到 ID 为 [" + assistant.getAgentConversationId() + "] 的会话内容"
        );
        SystemException.isTrue(
                AgentConversationTypeEnum.WORKSPACE_CONVERSATION.equals(conversation.getType()),
                "当前会话类型不支持执行 agent"
        );

        AgentConversationEntity workspace = Objects.requireNonNull(
                getAgentRecordStore().getConversation(assistant.getPrincipal(), conversation.getParentId(), execution.getDeviceIdentified()),
                "找不到 ID 为 [" + conversation.getParentId() + "] 的工作空间"
        );
        SystemException.isTrue(
                !AgentConversationTypeEnum.WORKSPACE_CONVERSATION.equals(workspace.getType()),
                "当前会话类型不支持执行 agent"
        );

        AgentMessageEntity userMessage = Objects.requireNonNull(
                execution.getUserMessage(),
                "找不到 ID 为 [" + assistant.getParentId() + "] 的用户消息记录"
        );

        ModelResolverMetadata modelResolverMetadata = modelSettingService.getModelMetadata(
                assistant.getModel(),
                userMessage.getMetadata()
        );
        Path userSkillDir = agentUserPluginToolkitService.registerUserMcp(
                modelResolverMetadata.getToolkit(),
                assistant.getPrincipal(),
                workspace.getId()
        );
        HubSkillRepository hubSkillRepository = agentHubSkillService.ensureCached(
                assistant.getPrincipal(),
                workspace.getId()
        );

        String workspacePath = AgentSandboxLayout.workspaceDirectory(
                Path.of(aiAppConfig.getWorkspacePath()),
                workspace.getId()
        ).toString();

        HarnessAgent.Builder builder = baseBuilder(execution, modelResolverMetadata, workspacePath)
                .skillRepository(new FileSystemSkillRepository(userSkillDir))
                .skillRepository(hubSkillRepository);

        return finishAgent(builder, execution);
    }

    @Override
    protected HarnessAgent.Builder attachFilesystem(
            HarnessAgent.Builder builder,
            AgentExecutionMetadata execution,
            String workspacePath
    ) {
        if (SandboxTypeEnum.DOCKER.equals(aiAppConfig.getSandboxType())) {
            return builder.filesystem(aiAppConfig.getDocker().toSpec());
        }
        Path directory = Path.of(workspacePath);
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new SystemException("创建云端工作区目录失败: " + directory, exception);
        }
        return builder.filesystem(new LocalFilesystemSpec().project(directory));
    }

    @Override
    protected AgentStateStore resolveStateStore(BasicAgentExecutionMetadata execution) {
        return new SandboxFileAgentStateStore(SandboxRecords.cloud(recordDirectory()), recordHome());
    }

    @Override
    protected SandboxRecords openRecords(String deviceIdentified) {
        return SandboxRecords.cloud(recordDirectory());
    }

    @Override
    protected String recordHome() {
        return recordDirectory().toString();
    }

    private Path recordDirectory() {
        return Path.of(aiAppConfig.getWorkspacePath()).toAbsolutePath().normalize();
    }
}
