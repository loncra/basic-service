package io.github.loncra.basic.service.ai.server.service.agent;

import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentWorkspaceResponseBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentWorkspaceEntity;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AgentManager {

    private final AgentWorkspaceService workspaceService;

    private final AgentConversationService conversationService;

    private final AgentMessageService messageService;

    private final AiAppConfig aiAppConfig;

    public AgentChatResponseBody chat(
            AgentChatRequestBody body,
            AuditAuthenticationToken token
    ) {
        AgentWorkspaceEntity workspace;
        if (Objects.nonNull(body.getAgentWorkspaceId())) {
            workspace = Objects.requireNonNull(workspaceService.get(body.getAgentWorkspaceId()),"找不到 ID 为 [" + body.getAgentWorkspaceId() + "] 的工作空间");
        } else {
            workspace = workspaceService.getDefaultWorkspace(token.getName());
        }

        AgentConversationEntity conversation;

        if (Objects.isNull(body.getAgentConversationId())) {
            conversation = new AgentConversationEntity();
            conversation.setAgentWorkspaceId(workspace.getId());
            conversation.setName(aiAppConfig.getNewConversation());
            conversation.setStatus(AgentChatStatusEnum.RUNNING);
            conversation.setPrincipal(token.getName());
            conversationService.insert(conversation);
        } else {
            conversation = Objects.requireNonNull(conversationService.get(body.getAgentConversationId()),"找不到 ID 为 [" + body.getAgentConversationId() + "] 的会话内容");
        }

        AgentMessageEntity userMessage = CastUtils.of(body, AgentMessageEntity.class);
        userMessage.setRole(AgentMessageRoleEnum.USER);
        userMessage.setPrincipal(token.getName());
        userMessage.setAgentConversationId(conversation.getId());
        messageService.insert(userMessage);

        AgentChatResponseBody responseBody = new AgentChatResponseBody();
        responseBody.setConversation(conversation);
        responseBody.setUserMessageId(userMessage.getId());

        return responseBody;
    }

    public Page<AgentWorkspaceEntity> my(
            MultiValueMap<String, Object> filter,
            PageRequest pageRequest
    ) {
        Page<AgentWorkspaceEntity> result = workspaceService.findPage(pageRequest, filter);
        List<AgentWorkspaceResponseBody> responseBodies = result.getElements()
                .stream()
                .map(s -> CastUtils.of(s, AgentWorkspaceResponseBody.class))
                .peek(s -> s.setConversations(conversationService.findByAgentWorkspaceId(s.getId())))
                .toList();
        return new Page<>(pageRequest, new LinkedList<>(responseBodies));
    }
}
