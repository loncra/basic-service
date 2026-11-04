package io.github.loncra.basic.service.ai.server.resolver.channel;

import io.agentscope.core.skill.repository.FileSystemSkillRepository;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.local.LocalFilesystem;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecuteMessageMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.ai.server.service.agent.AgentHubSkillService;
import io.github.loncra.basic.service.ai.server.service.agent.AgentUserPluginToolkitService;
import io.github.loncra.basic.service.ai.server.service.agent.HubSkillRepository;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CloudAgentChannelResolver extends AbstractAgentChannelResolver {

    private final AgentUserPluginToolkitService agentUserPluginToolkitService;

    private final AgentHubSkillService agentHubSkillService;

    @Override
    public ExecutionChannelEnum channel() {
        return ExecutionChannelEnum.MYSQL;
    }

    @Override
    public AgentChatResponseBody chat(
            AgentChatRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        AgentConversationEntity conversation;

        if (Objects.isNull(body.getAgentConversationId())) {
            conversation = conversationService.getDefaultWorkspace(token.getName());
        } else {
            conversation = Objects.requireNonNull(
                    conversationService.get(body.getAgentConversationId()),
                    "找不到 ID 为 [" + body.getAgentConversationId() + "] 的会话内容"
            );
        }

        ModelSettingMetadata modelMetadata = resolveModelMetadata(body.getModelId());

        if (conversation.getType() != AgentConversationTypeEnum.WORKSPACE_CONVERSATION) {
            Long parentId = conversation.getId();

            conversation = new AgentConversationEntity();
            conversation.setType(AgentConversationTypeEnum.WORKSPACE_CONVERSATION);
            conversation.setParentId(parentId);
            conversation.setPrincipal(token.getName());
            conversation.setMetadata(conversationService.get(parentId).getMetadata());
        } else {
            assertNoRunningAssistant(conversation.getId());
        }

        prepareConversation(conversation, body, modelMetadata);

        conversationService.save(conversation);

        AgentMessageEntity userMessage = buildUserMessage(body, modelMetadata, conversation.getId(), token.getName());
        messageService.insert(userMessage);

        AgentMessageEntity assistantMessage = buildAssistantMessage(userMessage, conversation.getId());
        messageService.insert(assistantMessage);

        agentSseStreamPublishResolver.sendExecuteMessage(
                AgentExecuteMessageMetadata.of(ExecutionChannelEnum.MYSQL, assistantMessage.getId().toString(), deviceIdentified)
        );

        AgentChatResponseBody responseBody = buildResponseBody(conversation, userMessage, assistantMessage);

        agentSseStreamPublishResolver.publishStreamStart(assistantMessage.getAgentConversationId().toString(), assistantMessage.getId().toString());

        return responseBody;
    }

    @Override
    public HarnessAgent createHarnessAgent(AgentMessageEntity assistant) {
        AgentConversationEntity conversation = conversationService.get(assistant.getAgentConversationId());
        SystemException.isTrue(
                AgentConversationTypeEnum.WORKSPACE_CONVERSATION.equals(conversation.getType()),
                "当前会话类型不支持执行 agent"
        );

        AgentConversationEntity workspace = Objects.requireNonNull(
                conversationService.get(conversation.getParentId()),
                "找不到 ID 为 [" + conversation.getParentId() + "] 的工作空间"
        );
        SystemException.isTrue(
                !AgentConversationTypeEnum.WORKSPACE_CONVERSATION.equals(workspace.getType()),
                "当前会话类型不支持执行 agent"
        );

        AgentMessageEntity userMessage = Objects.requireNonNull(
                messageService.get(assistant.getParentId()),
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

        String workspacePath = aiAppConfig.getWorkspacePath() + File.separator + workspace.getId();

        HarnessAgent.Builder builder = baseBuilder(assistant, modelResolverMetadata, workspacePath)
                .skillRepository(new FileSystemSkillRepository(userSkillDir))
                .skillRepository(hubSkillRepository);

        return finishAgent(builder, assistant);
    }

    @Override
    public AbstractFilesystem createFilesystem(AgentMessageEntity assistant, String workspacePath) {
        return new LocalFilesystem(workspacePath);
    }

    private void assertNoRunningAssistant(Long conversationId) {
        Long count = messageService.lambdaQuery()
                .eq(AgentMessageEntity::getAgentConversationId, conversationId)
                .eq(AgentMessageEntity::getRole, AgentMessageRoleEnum.ASSISTANT)
                .eq(AgentMessageEntity::getStatus, AgentChatStatusEnum.RUNNING)
                .count();
        SystemException.isTrue(Objects.isNull(count) || count <= 0, "当前会话仍有进行中的助手回复，请稍后再试");
    }
}
