package io.github.loncra.basic.service.ai.server.resolver.channel;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.agentscope.core.skill.repository.FileSystemSkillRepository;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.local.LocalFilesystem;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatBasicResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentResumeRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentChatMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecuteMessageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.BasicAgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.ai.server.service.agent.*;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CloudAgentChannelResolver extends AbstractAgentChannelResolver {

    private final AgentUserPluginToolkitService agentUserPluginToolkitService;

    private final AgentHubSkillService agentHubSkillService;

    private final AgentConversationService conversationService;

    private final AgentMessageService messageService;

    @Override
    public ExecutionChannelEnum getChannel() {
        return ExecutionChannelEnum.CLOUD;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentChatResponseBody chat(
            AgentChatRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        return super.chat(body, token, deviceIdentified);
    }

    @Override
    protected AgentConversationEntity getDefaultWorkspace(
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        return conversationService.getDefaultWorkspace(token.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
    protected List<AgentConversationEntity> doFindConversations(
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        return conversationService.lambdaQuery()
                .eq(AgentConversationEntity::getPrincipal, token.getName())
                .orderByDesc(IdEntity::getId)
                .list();
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
                conversationService.get(conversation.getParentId()),
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

        String workspacePath = aiAppConfig.getWorkspacePath() + File.separator + workspace.getId();

        HarnessAgent.Builder builder = baseBuilder(execution, modelResolverMetadata, workspacePath)
                .skillRepository(new FileSystemSkillRepository(userSkillDir))
                .skillRepository(hubSkillRepository);

        return finishAgent(builder, execution);
    }

    @Override
    public AbstractFilesystem createFilesystem(
            AgentExecutionMetadata execution,
            String workspacePath
    ) {
        return new LocalFilesystem(workspacePath);
    }

    @Override
    protected AgentStateStore resolveStateStore(BasicAgentExecutionMetadata execution) {
        return agentStateStore;
    }

    @Override
    public void updateConversation(
            AgentConversationEntity conversation,
            String deviceIdentified
    ) {
        conversationService.updateById(conversation);
    }

    @Override
    public void insertConversation(
            AgentConversationEntity conversation,
            String deviceIdentified
    ) {
        conversationService.insert(conversation);
    }

    @Override
    public AgentConversationEntity getConversation(
            String principal,
            String conversationId,
            String deviceIdentified
    ) {
        return conversationService.get(conversationId);
    }

    @Override
    public void saveMessage(
            AgentMessageEntity message,
            String deviceIdentified
    ) {
        messageService.insert(message);
    }

    @Override
    public AgentMessageEntity getMessage(
            String principal,
            String messageId,
            String deviceIdentified
    ) {
        return messageService.get(messageId);
    }

    @Override
    public List<AgentMessageEntity> getConversationMessages(
            String principal,
            String conversationId,
            String deviceIdentified
    ) {
        return messageService.findByConversationId(conversationId);
    }

    @Override
    public List<AgentMessageEntity> findRequestStopAssistantMessage(
            String principal,
            String conversationId,
            String deviceIdentified
    ) {
        return messageService.findRequestStopAssistantMessage(conversationId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(
            List<String> ids,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        super.deleteConversation(ids, token, deviceIdentified);
    }

    @Override
    protected void doDeleteConversation(
            AgentConversationEntity conversation,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        conversationService.deleteByEntity(conversation);
        messageService.findByConversationId(conversation.getId()).forEach(messageService::deleteByEntity);
        conversationService.lambdaQuery()
                .eq(AgentConversationEntity::getParentId, conversation.getId())
                .list()
                .forEach(e -> this.deleteConversation(e.getId(), token, deviceIdentified));
    }

    @Override
    public void saveAssistant(
            AgentExecutionMetadata execution,
            AgentMessageEntity assistant
    ) {
        messageService.lambdaUpdate()
                .set(AgentMessageEntity::getStatus, assistant.getStatus().getValue())
                .set(AgentChatMetadata::getContent, assistant.obtainContentJsonString())
                .set(AgentChatMetadata::getMetadata, assistant.obtainMetadataJsonString())
                .eq(AgentMessageEntity::getId, assistant.getId())
                .update();
    }

    @Override
    public void saveConversationStatus(
            AgentExecutionMetadata execution,
            AgentChatStatusEnum status
    ) {
        conversationService.lambdaUpdate()
                .set(AgentConversationEntity::getStatus, status.getValue())
                .eq(IdEntity::getId, execution.getAssistantMessage().getAgentConversationId())
                .update();
    }

    @Override
    public Page<AgentMessageEntity> histories(
            PageRequest request,
            MultiValueMap<String, Object> filter,
            String conversationId,
            boolean totalPage,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        AgentConversationEntity conversation = Objects.requireNonNull(
                conversationService.get(conversationId),
                "找不到 ID 为 [" + conversationId + "] 的会话内容"
        );
        PrincipalDetailsConstants.equals(conversation, token);

        QueryWrapper<AgentMessageEntity> wrapper = messageService.getQueryGenerator()
                .createQueryWrapperFromMap(filter);
        wrapper.eq(AgentMessageEntity.CONVERSATION_ID_TABLE_FIELD_NAME, conversationId)
                .orderByDesc(IdEntity.ID_FIELD_NAME);

        return totalPage ? messageService.findTotalPage(request, wrapper) : messageService.findPage(request, wrapper);
    }

    @Override
    public int positioningMessagePageNumber(
            String principal,
            String conversationId,
            String messageId,
            int pageSize,
            String deviceIdentified
    ) {
        return messageService.positioningPageNumber(conversationId, messageId, pageSize);
    }

    @Override
    public void updateConversationGenerateNameStatus(
            String id,
            ExecuteStatus status,
            AgentExecutionMetadata executionMetadata
    ) {
        conversationService.lambdaUpdate()
                .set(AgentConversationEntity::getGenerateNameStatus, status.getValue())
                .eq(IdEntity::getId, id)
                .update();
    }

    @Override
    public void updateAssistantMetadata(
            AgentMessageEntity assistantEntity,
            AgentExecutionMetadata executionMetadata
    ) {
        messageService.lambdaUpdate()
                .set(AgentMessageEntity::getMetadata, assistantEntity.obtainMetadataJsonString())
                .eq(AgentMessageEntity::getId, assistantEntity.getId())
                .update();
    }

    @Override
    public void updateGenerateName(
            AgentConversationEntity conversation,
            AgentExecutionMetadata executionMetadata
    ) {
        conversationService.lambdaUpdate()
                .set(AgentConversationEntity::getGenerateNameStatus, conversation.getGenerateNameStatus())
                .set(AgentConversationEntity::getName, conversation.getName())
                .eq(IdEntity::getId, conversation.getId())
                .update();
    }
}
