package io.github.loncra.basic.service.ai.server.service.agent;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.github.loncra.basic.service.ai.api.constants.AiMqConstants;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.entity.ModelSettingEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentAssistantMessageContent;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentStatusChangeContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentTextContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.basic.service.ai.server.interceptor.AgentStreamEventInterceptor;
import io.github.loncra.basic.service.ai.server.resolver.AgentEventResolver;
import io.github.loncra.basic.service.ai.server.resolver.AgentSseStreamPublishResolver;
import io.github.loncra.basic.service.ai.server.resolver.event.AbstractAgentEventResolver;
import io.github.loncra.basic.service.ai.server.service.ModelSettingService;
import io.github.loncra.basic.service.ai.server.utils.ReactorContextUtils;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.chat.TextMessageMetadata;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.idempotent.annotation.Concurrent;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentManager {


    public final static String CONCURRENT_PREFIX = "loncra:basic-service:ai:app:agent:chat:concurrent:";

    private final AmqpTemplate amqpTemplate;

    private final AgentConversationService conversationService;

    @Getter
    private final AgentMessageService messageService;

    private final ModelSettingService modelSettingService;

    private final AgentSseStreamPublishResolver agentSseStreamPublishResolver;

    private final List<AgentEventResolver> agentEventResolvers;

    private final List<AgentStreamEventInterceptor> agentStreamEventInterceptors;

    @Concurrent(
            value = CONCURRENT_PREFIX + "[#body.agentConversationId]",
            condition = "[#body.agentConversationId != null]"
    )
    @Transactional(rollbackFor = Exception.class)
    public AgentChatResponseBody chat(
            AgentChatRequestBody body,
            AuditAuthenticationToken token
    ) {

        if (Objects.nonNull(body.getAgentConversationId())) {
            SystemException.isTrue(agentSseStreamPublishResolver.isCompleted(body.getAgentConversationId().toString()), "该会话正在应答。");
        }

        AgentConversationEntity conversation;

        if (Objects.isNull(body.getAgentConversationId())) {
            conversation = conversationService.getDefaultWorkspace(token.getName());
        } else {
            conversation = Objects.requireNonNull(
                    conversationService.get(body.getAgentConversationId()),
                    "找不到 ID 为 [" + body.getAgentConversationId() + "] 的会话内容"
            );
        }

        if (conversation.getType() != AgentConversationTypeEnum.WORKSPACE_CONVERSATION) {
            Long parentId = conversation.getId();

            conversation = new AgentConversationEntity();
            conversation.setStatus(AgentChatStatusEnum.RUNNING);
            conversation.setType(AgentConversationTypeEnum.WORKSPACE_CONVERSATION);
            conversation.setParentId(parentId);
            conversation.setGenerateName(YesOrNo.No);
            conversation.setPrincipal(token.getName());
            conversationService.insert(conversation);
        } else {
            assertNoRunningAssistant(conversation.getId());
            conversation.setStatus(AgentChatStatusEnum.RUNNING);
            conversationService.updateById(conversation);
        }

        ModelSettingEntity model = Objects.requireNonNull(modelSettingService.get(body.getModelId()), "找不到 ID 为 [" + body.getModelId() + "] 的模型数据");

        AgentMessageEntity userMessage = CastUtils.of(body, AgentMessageEntity.class);
        userMessage.setModel(CastUtils.of(model, ModelSettingMetadata.class));
        userMessage.setRole(AgentMessageRoleEnum.USER);
        userMessage.setPrincipal(token.getName());
        userMessage.setStatus(AgentChatStatusEnum.READY);
        userMessage.setAgentConversationId(conversation.getId());
        messageService.insert(userMessage);

        AgentMessageEntity assistantMessage = new AgentMessageEntity();
        assistantMessage.setRole(AgentMessageRoleEnum.ASSISTANT);
        assistantMessage.setPrincipal(token.getName());
        assistantMessage.setModel(CastUtils.of(model, ModelSettingMetadata.class));
        assistantMessage.setType(userMessage.getType());
        assistantMessage.setStatus(AgentChatStatusEnum.RUNNING);
        assistantMessage.setAgentConversationId(conversation.getId());
        assistantMessage.setParentId(userMessage.getId());
        assistantMessage.setContent(new LinkedList<>());
        messageService.insert(assistantMessage);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                amqpTemplate.convertAndSend(
                        SystemConstants.SYS_AI_RABBITMQ_EXCHANGE,
                        AiMqConstants.AGENT_STREAM_QUEUE,
                        assistantMessage.getId()
                );
            }
        });

        AgentChatResponseBody responseBody = new AgentChatResponseBody();
        responseBody.setConversation(conversation);
        responseBody.setUserMessageId(userMessage.getId());
        responseBody.setAssistantId(assistantMessage.getId());

        agentSseStreamPublishResolver.publish(conversation.getId().toString(), new AgentAssistantMessageContent());

        return responseBody;
    }

    public Flux<ServerSentEvent<String>> stream(
            Long assistantId,
            AuditAuthenticationToken token
    ) {
        AgentMessageEntity assistant = messageService.get(assistantId);
        SystemException.isTrue(Objects.nonNull(assistant), "找不到 ID 为 [" + assistantId + "] 的助手消息");
        SystemException.isTrue(AgentMessageRoleEnum.ASSISTANT.equals(assistant.getRole()), "ID 为 [" + assistantId + "] 的消息记录非助手消息");
        PrincipalDetailsConstants.equals(assistant, token);
        if (AgentChatStatusEnum.COMPLETED.equals(assistant.getStatus())) {
            return Flux.fromIterable(agentSseStreamPublishResolver.getAgentMessageServerSentEvent(assistant));
        }
        return agentSseStreamPublishResolver.open(assistant);
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

    private void assertNoRunningAssistant(Long conversationId) {
        Long count = messageService.lambdaQuery()
                .eq(AgentMessageEntity::getAgentConversationId, conversationId)
                .eq(AgentMessageEntity::getRole, AgentMessageRoleEnum.ASSISTANT)
                .eq(AgentMessageEntity::getStatus, AgentChatStatusEnum.RUNNING)
                .count();
        SystemException.isTrue(Objects.isNull(count) || count <= 0, "当前会话仍有进行中的助手回复，请稍后再试");
    }

    public void execute(AgentMessageEntity assistant) {
        AgentMessageEntity userMessage = Objects.requireNonNull(messageService.get(assistant.getParentId()), "找不到 ID 为 [" + assistant.getParentId() + "] 的用户消息记录");
        String prompt = TextMessageMetadata.ofString(userMessage.getContent());

        ReActAgent agent = modelSettingService.getAgent(assistant.getModel(), userMessage.getMetadata());

        RuntimeContext context = RuntimeContext.builder()
                .userId(assistant.getPrincipal())
                .sessionId(String.valueOf(assistant.getAgentConversationId()))
                .put(SecurityContext.class, SecurityContextHolder.getContext())
                .build();

        Flux<AgentAssistantMessageContent> flux =  agent.streamEvents(prompt, context)
                .concatMap(e -> Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> convertEventToMessageContent(e, assistant, context))))
                .filter(Objects::nonNull)
                .concatWith(Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> postStreamEvent(assistant))))
                .concatWith(Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> onCompleted(assistant))))
                .onErrorResume(t -> Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> onError(t, assistant))));

        ReactorContextUtils.captureContext(flux).subscribe(
                s -> publishStreamEvent(s, assistant),
                error -> onError(error, assistant)
        );
    }

    private void publishStreamEvent(
            AgentAssistantMessageContent message,
            AgentMessageEntity assistant
    ) {
        agentSseStreamPublishResolver.publish(assistant.getAgentConversationId().toString(), message);

        if (Objects.nonNull(message.getEventSource())) {
            agentEventResolvers.stream()
                    .filter(s -> s.isSupport(message.getEventSource()))
                    .filter(s -> AbstractAgentEventResolver.class.isAssignableFrom(s.getClass()))
                    .map(s -> CastUtils.cast(s, AbstractAgentEventResolver.class))
                    .forEach(resolver -> resolverPostPublish(resolver, message,assistant));
        }
    }

    private void resolverPostPublish(
            AbstractAgentEventResolver<AgentAssistantMessageContent> resolver,
            AgentAssistantMessageContent message,
            AgentMessageEntity assistant
    ) {
        boolean cleanStream = resolver.postPublish(message, assistant);
        if (cleanStream) {
            agentSseStreamPublishResolver.clean(assistant.getAgentConversationId().toString(), message.getSseEventId());
        }
    }

    private Flux<AgentAssistantMessageContent> onCompleted(
            AgentMessageEntity assistant
    ) {
        AgentTextContentMetadata textContent = AgentTextContentMetadata.of(
                AgentMessageContentTypeEnum.COMPLETED,
                assistant.getId().toString(),
                StringUtils.EMPTY
        );

        return Flux.just(textContent);
    }

    public Flux<AgentAssistantMessageContent> onError(
            Throwable t,
            AgentMessageEntity assistant
    ) {
        log.error("助手消息 [{}] 执行失败", assistant.getId(), t);
        AgentAssistantMessageContent textContent = AgentTextContentMetadata.of(
                AgentMessageContentTypeEnum.ERROR,
                UUID.randomUUID().toString(),
                t.getMessage()
        );
        assistant.updateContent(textContent);
        assistant.setStatus(AgentChatStatusEnum.FAILED);

        messageService.lambdaUpdate()
                .set(AgentMessageEntity::getStatus, AgentChatStatusEnum.FAILED.getValue())
                .set(AgentMessageEntity::getContent, assistant.obtainContentJsonString())
                .eq(AgentMessageEntity::getId, assistant.getId())
                .update();

        AgentStatusChangeContentMetadata agentEndContent = new AgentStatusChangeContentMetadata();
        agentEndContent.setId(assistant.getAgentConversationId().toString());
        agentEndContent.setStatus(AgentChatStatusEnum.FAILED);

        conversationService.lambdaUpdate()
                .set(AgentConversationEntity::getStatus, AgentChatStatusEnum.FAILED.getValue())
                .eq(IdEntity::getId, assistant.getAgentConversationId())
                .update();

        return Flux.just(agentEndContent, textContent);
    }

    private Flux<AgentAssistantMessageContent> postStreamEvent(
            AgentMessageEntity assistant
    ) {

        return Flux.fromIterable(agentStreamEventInterceptors)
                .concatMap(interceptor -> Flux.fromIterable(interceptor.postEventsStream(assistant))
                        .doOnError(e -> log.error("助手消息 [{}] 拦截器 [{}] 执行失败", assistant.getId(), interceptor.getClass().getSimpleName(), e))
                        .onErrorResume(e -> Flux.empty())
                );
    }

    private Flux<AgentAssistantMessageContent> convertEventToMessageContent(
            AgentEvent event,
            AgentMessageEntity assistant,
            RuntimeContext context
    ) {
        if (log.isDebugEnabled()) {
            log.debug("助手消息 [{}] 收到事件 {}", assistant.getId(), CastUtils.convertValue(event, CastUtils.MAP_TYPE_REFERENCE));
        }

        List<AgentAssistantMessageContent> contents = agentEventResolvers.stream()
                .filter(s -> s.isSupport(event))
                .map(s -> s.process(assistant, event, context))
                .toList();

        return Flux.fromIterable(contents);
    }
}
