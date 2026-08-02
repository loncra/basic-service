package io.github.loncra.basic.service.ai.server.service.agent;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ConfirmResult;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.ToolCallState;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.github.loncra.basic.service.ai.api.constants.AiMqConstants;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatBasicResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentResumeRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.ModelSettingEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentChatMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentStatusContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.CustomizeMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ToolCallBlockContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.*;
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
import io.github.loncra.framework.commons.RestResult;
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

import java.io.File;
import java.util.*;

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

    private final AiAppConfig aiAppConfig;

    private final AgentStateStore agentStateStore;

    @Concurrent(
            value = CONCURRENT_PREFIX + "[#body.agentConversationId]",
            condition = "[#body.agentConversationId != null]"
    )
    @Transactional(rollbackFor = Exception.class)
    public AgentChatResponseBody chat(
            AgentChatRequestBody body,
            AuditAuthenticationToken token
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
        responseBody.setAssistantMessageId(assistantMessage.getId());

        CustomizeMetadata metadata = new CustomizeMetadata();
        metadata.setId(assistantMessage.getId().toString());
        metadata.setEventType(AgentMessageContentTypeEnum.STREAM_START);

        agentSseStreamPublishResolver.publish(conversation.getId().toString(), metadata);

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

        RuntimeContext context = RuntimeContext.builder()
                .userId(assistant.obtainUserId())
                .sessionId(String.valueOf(assistant.getAgentConversationId()))
                .put(SecurityContext.class, SecurityContextHolder.getContext())
                .build();
        AgentMessageEntity userMessage = Objects.requireNonNull(messageService.get(assistant.getParentId()), "找不到 ID 为 [" + assistant.getParentId() + "] 的用户消息记录");
        String prompt = TextMessageMetadata.ofString(userMessage.getContent());
        try (HarnessAgent agent = createHarnessAgent(assistant)) {
            execute(agent, context, Collections.singletonList(Msg.builder().role(MsgRole.USER).textContent(prompt).build()), assistant);
        }

    }

    private void execute(
            HarnessAgent agent,
            RuntimeContext context,
            List<Msg> messages,
            AgentMessageEntity assistant
    ) {
        Flux<AbstractAssistantMessageContentMetadata> flux;
        try {
            flux = agent.streamEvents(messages, context)
                    .concatMap(e -> Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> convertEventToMessageContent(e, assistant, context))))
                    .filter(Objects::nonNull)
                    .concatWith(Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> postStreamEvent(assistant))))
                    .concatWith(Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> onCompleted(assistant))))
                    .onErrorResume(t -> Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> onError(t, assistant))));

        } catch (Exception e) {
            log.error("助手消息 [{}] 在执行应答前出现异常", assistant.getId(), e);
            // 直接走 onError 兜底，内部会发布 ERROR + STREAM_END
            flux = Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(
                    ctxView,
                    () -> onError(e, assistant)
            ));
        }

        ReactorContextUtils.captureContext(flux).subscribe(
                s -> publishStreamEvent(s, assistant),
                error -> onError(error, assistant)
        );
    }

    private HarnessAgent createHarnessAgent(AgentMessageEntity assistant) {
        AgentConversationEntity conversation = conversationService.get(assistant.getAgentConversationId());
        SystemException.isTrue(AgentConversationTypeEnum.WORKSPACE_CONVERSATION.equals(conversation.getType()), "当前会话类型不支持执行 agent");

        AgentConversationEntity workspace = Objects.requireNonNull(conversationService.get(conversation.getParentId()), "找不到 ID 为 [" + conversation.getParentId() + "] 的工作空间");
        SystemException.isTrue(!AgentConversationTypeEnum.WORKSPACE_CONVERSATION.equals(workspace.getType()), "当前会话类型不支持执行 agent");

        AgentMessageEntity userMessage = Objects.requireNonNull(messageService.get(assistant.getParentId()), "找不到 ID 为 [" + assistant.getParentId() + "] 的用户消息记录");

        ModelResolverMetadata modelResolverMetadata = modelSettingService.getModelMetadata(assistant.getModel(), userMessage.getMetadata());
        HarnessAgent.Builder builder = HarnessAgent.builder()
                .name(assistant.getType().toString())
                .sysPrompt(aiAppConfig.getSystemPrompt())
                .model(modelResolverMetadata.getModel())
                .toolkit(modelResolverMetadata.getToolkit())
                .stateStore(agentStateStore)
                .compaction(aiAppConfig.toCompactionConfig())
                .workspace(aiAppConfig.getWorkspacePath() + File.separator + workspace.getId());
        if (!AgentChatTypeEnum.ASK.equals(assistant.getType())) {
            builder.enablePlanMode()
                    .planFileDirectory(assistant.getType().toString());
        }
        return builder.build();
    }

    private void publishStreamEvent(
            AbstractAssistantMessageContentMetadata message,
            AgentMessageEntity assistant
    ) {
        agentSseStreamPublishResolver.publish(assistant.getAgentConversationId().toString(), message);

        if (Objects.nonNull(message.getEventSource())) {
            agentEventResolvers.stream()
                    .filter(s -> s.isSupport(message.getEventSource()))
                    .filter(s -> AbstractAgentEventResolver.class.isAssignableFrom(s.getClass()))
                    .map(s -> CastUtils.cast(s, AbstractAgentEventResolver.class))
                    .forEach(resolver -> resolverPostPublish(resolver, message, assistant));
        }
    }

    private void resolverPostPublish(
            AbstractAgentEventResolver<AbstractAssistantMessageContentMetadata> resolver,
            AbstractAssistantMessageContentMetadata message,
            AgentMessageEntity assistant
    ) {
        boolean cleanStream = resolver.postPublish(message, assistant);
        if (cleanStream) {
            agentSseStreamPublishResolver.clean(assistant.getAgentConversationId().toString(), message.getSseEventId());
        }
    }

    private Flux<AbstractAssistantMessageContentMetadata> onCompleted(
            AgentMessageEntity assistant
    ) {

        CustomizeMetadata content = new CustomizeMetadata();
        content.setEventType(AgentMessageContentTypeEnum.STREAM_END);
        content.setId(assistant.getId().toString());
        content.getMetadata().put(SystemConstants.STATUS_TABLE_FIELD_NAME, assistant.getStatus());

        return Flux.just(content);
    }

    public Flux<AbstractAssistantMessageContentMetadata> onError(
            Throwable t,
            AgentMessageEntity assistant
    ) {
        log.error("助手消息 [{}] 执行失败", assistant.getId(), t);

        CustomizeMetadata content = new CustomizeMetadata();
        content.setEventType(AgentMessageContentTypeEnum.ERROR);
        content.setId(assistant.getId().toString());
        content.getMetadata().put(RestResult.DEFAULT_MESSAGE_NAME, t.getMessage());
        assistant.updateContent(content);
        assistant.setStatus(AgentChatStatusEnum.FAILED);

        messageService.lambdaUpdate()
                .set(AgentMessageEntity::getStatus, AgentChatStatusEnum.FAILED.getValue())
                .set(AgentMessageEntity::getContent, assistant.obtainContentJsonString())
                .eq(AgentMessageEntity::getId, assistant.getId())
                .update();

        AgentStatusContentMetadata agentEndContent = new AgentStatusContentMetadata();
        agentEndContent.setId(assistant.getAgentConversationId().toString());
        agentEndContent.setStatus(AgentChatStatusEnum.FAILED);

        conversationService.lambdaUpdate()
                .set(AgentConversationEntity::getStatus, AgentChatStatusEnum.FAILED.getValue())
                .eq(IdEntity::getId, assistant.getAgentConversationId())
                .update();
        return Flux.just(agentEndContent, content).concatWith(onCompleted(assistant));
    }

    private Flux<AbstractAssistantMessageContentMetadata> postStreamEvent(
            AgentMessageEntity assistant
    ) {

        return Flux.fromIterable(agentStreamEventInterceptors)
                .concatMap(interceptor -> Flux.fromIterable(interceptor.postEventsStream(assistant))
                        .doOnError(e -> log.error("助手消息 [{}] 拦截器 [{}] 执行失败", assistant.getId(), interceptor.getClass().getSimpleName(), e))
                );
    }

    private Flux<AbstractAssistantMessageContentMetadata> convertEventToMessageContent(
            AgentEvent event,
            AgentMessageEntity assistant,
            RuntimeContext context
    ) {
        if (log.isDebugEnabled()) {
            log.debug("助手消息 [{}] 收到事件 {}", assistant.getId(), CastUtils.convertValue(event, CastUtils.MAP_TYPE_REFERENCE));
        }

        List<AbstractAssistantMessageContentMetadata> contents = agentEventResolvers.stream()
                .filter(s -> s.isSupport(event))
                .flatMap(s -> s.process(assistant, event, context).stream())
                .toList();

        return Flux.fromIterable(contents);
    }

    public AgentChatBasicResponseBody resume(
            AgentResumeRequestBody body,
            AuditAuthenticationToken token
    ) {
        AgentMessageEntity assistantMessage = Objects.requireNonNull(messageService.get(body.getAssistantMessageId()));

        RuntimeContext context = RuntimeContext.builder()
                .userId(assistantMessage.obtainUserId())
                .sessionId(String.valueOf(assistantMessage.getAgentConversationId()))
                .put(SecurityContext.class, SecurityContextHolder.getContext())
                .build();

        List<ToolUseBlock> toolUseBlocks = assistantMessage.obtainMessageContents().stream()
                .filter(s -> AgentMessageContentTypeEnum.TOOL_CALL.getValue().equals(s.getType()))
                .map(s -> CastUtils.cast(s, ToolCallBlockContentMetadata.class))
                .filter(s -> ToolCallState.PENDING.equals(s.getHitlStatus()))
                .map(ToolCallBlockContentMetadata::toToolUseBlock)
                .toList();

        List<ConfirmResult> confirmResults = new LinkedList<>();
        for (ToolUseBlock toolUseBlock : toolUseBlocks) {
            Boolean confirm = body.getConfirmResults()
                    .stream()
                    .filter(s -> s.getToolCallId().equals(toolUseBlock.getId()))
                    .findFirst()
                    .map(AgentResumeRequestBody.ConfirmResult::isConfirmed)
                    .orElse(null);
            SystemException.isTrue(Objects.nonNull(confirm), "ID 为 [" + toolUseBlock.getId() + "] 的工具需要确认");

            confirmResults.add(new ConfirmResult(confirm, toolUseBlock));
            Optional<ToolCallBlockContentMetadata> optional = assistantMessage
                    .obtainMessageContents()
                    .stream()
                    .filter(s -> s.getId().equals(toolUseBlock.getId()))
                    .map(s -> CastUtils.cast(s, ToolCallBlockContentMetadata.class))
                    .findFirst();
            if (optional.isPresent()) {
                ToolCallBlockContentMetadata contentMetadata = optional.get();
                contentMetadata.setUserConfirmed(confirm);
                assistantMessage.updateContent(contentMetadata);
            }

        }

        messageService.lambdaUpdate()
                .set(AgentChatMetadata::getContent, assistantMessage.obtainContentJsonString())
                .eq(AgentMessageEntity::getId, assistantMessage.getId())
                .update();

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put(Msg.METADATA_CONFIRM_RESULTS, confirmResults);
        Msg resumeMsg = Msg.builderForRole(MsgRole.USER).metadata(metadata).build();

        execute(createHarnessAgent(assistantMessage), context, Collections.singletonList(resumeMsg), assistantMessage);

        AgentChatBasicResponseBody result = new AgentChatBasicResponseBody();
        result.setAssistantMessageId(assistantMessage.getId());
        result.setUserMessageId(assistantMessage.getParentId());
        return result;
    }
}
