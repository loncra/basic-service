package io.github.loncra.basic.service.ai.server.service.agent;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentChatMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AgentManager {

    private final AgentConversationService conversationService;

    private final AgentMessageService messageService;

    private final AiAppConfig aiAppConfig;

    public AgentChatResponseBody chat(
            AgentChatMetadata body,
            AuditAuthenticationToken token
    ) {

        AgentConversationEntity conversation;

        if (Objects.isNull(body.getAgentConversationId())) {
            conversation = conversationService.getDefaultWorkspace(token.getName());
        } else {
            conversation = Objects.requireNonNull(conversationService.get(body.getAgentConversationId()),"找不到 ID 为 [" + body.getAgentConversationId() + "] 的会话内容");
        }

        if (conversation.getType() != AgentConversationTypeEnum.WORKSPACE_CONVERSATION) {
            Long parentId = conversation.getId();

            conversation = new AgentConversationEntity();
            conversation.setName(aiAppConfig.getNewConversation());
            conversation.setStatus(AgentChatStatusEnum.RUNNING);
            conversation.setType(AgentConversationTypeEnum.WORKSPACE_CONVERSATION);
            conversation.setParentId(parentId);
            conversation.setPrincipal(token.getName());
            conversationService.insert(conversation);
        }

        AgentMessageEntity userMessage = CastUtils.of(body, AgentMessageEntity.class);
        userMessage.setRole(AgentMessageRoleEnum.USER);
        userMessage.setPrincipal(token.getName());
        userMessage.setStatus(AgentChatStatusEnum.READY);
        userMessage.setAgentConversationId(conversation.getId());
        messageService.insert(userMessage);

        AgentMessageEntity assistantMessage = new AgentMessageEntity();
        assistantMessage.setRole(AgentMessageRoleEnum.ASSISTANT);
        assistantMessage.setPrincipal(token.getName());
        assistantMessage.setModelId(userMessage.getModelId());
        assistantMessage.setStatus(AgentChatStatusEnum.RUNNING);
        assistantMessage.setAgentConversationId(conversation.getId());
        assistantMessage.setParentId(userMessage.getId());
        messageService.insert(assistantMessage);

        AgentChatResponseBody responseBody = new AgentChatResponseBody();
        responseBody.setConversation(conversation);
        responseBody.setUserMessageId(userMessage.getId());
        responseBody.setAssistantId(assistantMessage.getId());

        return responseBody;
    }


    public Page<AgentMessageEntity> histories(
            PageRequest request,
            MultiValueMap<String, Object> filter,
            Long conversationId,
            boolean totalPage,
            AuditAuthenticationToken token
    ) {
        AgentConversationEntity conversation = conversationService.get(conversationId);
        PrincipalDetailsConstants.equals(conversation, token);
        QueryWrapper<AgentMessageEntity> wrapper = messageService.getQueryGenerator()
                .createQueryWrapperFromMap(filter);
        wrapper.eq(AgentMessageEntity.CONVERSATION_ID_TABLE_FIELD_NAME, conversationId)
                .orderByDesc(IdEntity.ID_FIELD_NAME);

        Page<AgentMessageEntity> page;
        if (totalPage) {
            page = messageService.findTotalPage(request, wrapper);
        } else {
            page = messageService.findPage(request, wrapper);
        }

        List<AgentMessageEntity> messages = page.getElements();
        if (CollectionUtils.isEmpty(messages)) {
            return page;
        }

        return page;
    }

    public int positioningMessagePageNumber(
            Long conversationId,
            Long messageId,
            int pageSize
    ) {
        return messageService.positioningPageNumber(conversationId, messageId, pageSize);
    }

}
