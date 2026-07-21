package io.github.loncra.basic.service.ai.server.service.agent;

import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AgentManager {

    private final AgentConversationService conversationService;

    private final AgentMessageService messageService;

    private final AiAppConfig aiAppConfig;

    public AgentChatResponseBody chat(
            AgentChatRequestBody body,
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

        /*AgentMessageEntity assistantMessage = new AgentMessageEntity();
        userMessage.setRole(AgentMessageRoleEnum.ASSISTANT);
        userMessage.setPrincipal(token.getName());
        userMessage.setStatus(AgentChatStatusEnum.RUNNING);
        userMessage.setAgentConversationId(conversation.getId());
        messageService.insert(userMessage);*/

        AgentChatResponseBody responseBody = new AgentChatResponseBody();
        responseBody.setConversation(conversation);
        responseBody.setUserMessageId(userMessage.getId());

        return responseBody;
    }


}
