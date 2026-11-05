package io.github.loncra.basic.service.ai.server.resolver.channel;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.agentscope.core.event.ConfirmResult;
import io.agentscope.core.skill.repository.FileSystemSkillRepository;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.local.LocalFilesystem;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
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
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.ai.server.service.agent.AgentHubSkillService;
import io.github.loncra.basic.service.ai.server.service.agent.AgentUserPluginToolkitService;
import io.github.loncra.basic.service.ai.server.service.agent.HubSkillRepository;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;
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
                AgentExecuteMessageMetadata.of(ExecutionChannelEnum.MYSQL, assistantMessage.getId().toString(), deviceIdentified),
                true
        );

        AgentChatResponseBody responseBody = buildResponseBody(conversation, userMessage, assistantMessage);

        agentSseStreamPublishResolver.publishStreamStart(assistantMessage.getAgentConversationId().toString(), assistantMessage.getId().toString());

        return responseBody;
    }

    @Override
    public AgentChatBasicResponseBody resume(
            AgentResumeRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        AgentMessageEntity assistantMessage = Objects.requireNonNull(messageService.get(body.getAssistantMessageId()));
        PrincipalDetailsConstants.equals(assistantMessage, token);
        SystemException.isTrue(
                Objects.equals(String.valueOf(assistantMessage.getAgentConversationId()), body.getClientConversationId()),
                "助手消息与会话不一致"
        );

        List<ConfirmResult> confirmResults = getResumeConfirmResults(body, token, deviceIdentified);

        assistantMessage.saveUserConfirmResultMetadata(confirmResults);
        applyConfirmResults(assistantMessage, confirmResults);

        messageService.lambdaUpdate()
                .set(AgentChatMetadata::getContent, assistantMessage.obtainContentJsonString())
                .set(AgentChatMetadata::getMetadata, assistantMessage.obtainMetadataJsonString())
                .eq(AgentMessageEntity::getId, assistantMessage.getId())
                .update();

        agentSseStreamPublishResolver.publishStreamStart(assistantMessage.getAgentConversationId().toString(), assistantMessage.getId().toString());
        agentSseStreamPublishResolver.sendExecuteMessage(
                AgentExecuteMessageMetadata.of(ExecutionChannelEnum.MYSQL, assistantMessage.getId().toString(), deviceIdentified),
                true
        );

        AgentChatBasicResponseBody result = new AgentChatBasicResponseBody();
        result.setAssistantMessageId(assistantMessage.getId());
        result.setUserMessageId(assistantMessage.getParentId());

        return result;
    }

    @Override
    public AgentExecutionMetadata getAgentExecutionMetadata(AgentExecuteMessageMetadata message) {
        AgentMessageEntity assistant = messageService.get(NumberUtils.toLong(message.getRef()));
        if (Objects.isNull(assistant)) {
            return null;
        }

        AgentMessageEntity userMessage = messageService.get(assistant.getParentId());
        AgentConversationEntity conversation = conversationService.get(assistant.getAgentConversationId());

        AgentExecutionMetadata execution = new AgentExecutionMetadata();
        execution.setChannel(ExecutionChannelEnum.MYSQL);
        execution.setDeviceIdentified(message.getDeviceIdentified());
        execution.setClientConversationId(assistant.getAgentConversationId().toString());
        execution.setConversation(conversation);
        execution.setUserMessage(userMessage);
        execution.setAssistantMessage(assistant);
        return execution;
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
    public AbstractFilesystem createFilesystem(AgentExecutionMetadata execution, String workspacePath) {
        return new LocalFilesystem(workspacePath);
    }

    @Override
    protected AgentStateStore resolveStateStore(BasicAgentExecutionMetadata execution) {
        return agentStateStore;
    }

    @Override
    public void saveConversation(AgentConversationEntity conversation, String deviceIdentified) {
        conversationService.save(conversation);
    }

    @Override
    public AgentConversationEntity getConversation(Long conversationId, String deviceIdentified) {
        return conversationService.get(conversationId);
    }

    @Override
    public void saveMessage(AgentMessageEntity message, String deviceIdentified) {
        messageService.insert(message);
    }

    @Override
    public AgentMessageEntity getMessage(Long messageId, String deviceIdentified) {
        return messageService.get(messageId);
    }

    @Override
    public List<AgentMessageEntity> getConversationMessages(Long conversationId, String deviceIdentified) {
        return messageService.findByConversationId(conversationId);
    }

    @Override
    public List<AgentMessageEntity> findRequestStopAssistantMessage(Long conversationId, String deviceIdentified) {
        return messageService.findRequestStopAssistantMessage(conversationId);
    }

    @Override
    public void deleteConversationData(Long conversationId, String deviceIdentified) {
    }

    @Override
    public void deleteConversation(List<Integer> ids, AuditAuthenticationToken token, String deviceIdentified) {
        conversationService.get(ids).forEach(entity -> deleteConversation(entity, token));
    }

    private void deleteConversation(AgentConversationEntity entity, AuditAuthenticationToken token) {
        PrincipalDetailsConstants.equals(entity, token);
        SystemException.isTrue(!AgentConversationTypeEnum.DEFAULT_WORKSPACE.equals(entity.getType()), "无法删除 [" + entity.getType().getName() + "]类型的空间");
        conversationService.lambdaQuery()
                .eq(AgentConversationEntity::getParentId, entity.getId())
                .list()
                .forEach(conversation -> deleteConversation(conversation, token));
        agentStateStore.delete(Strings.CS.replace(entity.getPrincipal(), CacheProperties.DEFAULT_SEPARATOR, CastUtils.UNDERSCORE), entity.getId().toString());
        agentSseStreamPublishResolver.remove(entity.getId().toString());
        conversationService.deleteByEntity(entity);
        messageService.findByConversationId(entity.getId()).forEach(messageService::deleteByEntity);
    }

    @Override
    public void saveAssistant(AgentExecutionMetadata execution, AgentMessageEntity assistant) {
        messageService.lambdaUpdate()
                .set(AgentMessageEntity::getStatus, assistant.getStatus().getValue())
                .set(AgentChatMetadata::getContent, assistant.obtainContentJsonString())
                .set(AgentChatMetadata::getMetadata, assistant.obtainMetadataJsonString())
                .eq(AgentMessageEntity::getId, assistant.getId())
                .update();
    }

    @Override
    public void saveConversationStatus(AgentExecutionMetadata execution, AgentChatStatusEnum status) {
        conversationService.lambdaUpdate()
                .set(AgentConversationEntity::getStatus, status.getValue())
                .eq(IdEntity::getId, execution.getAssistantMessage().getAgentConversationId())
                .update();
    }

    @Override
    public Page<AgentMessageEntity> histories(
            PageRequest request,
            MultiValueMap<String, Object> filter,
            Long conversationId,
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
    public int positioningMessagePageNumber(Long conversationId, Long messageId, int pageSize, String deviceIdentified) {
        return messageService.positioningPageNumber(conversationId, messageId, pageSize);
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
