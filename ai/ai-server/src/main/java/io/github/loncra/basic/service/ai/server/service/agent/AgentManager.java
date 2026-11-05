package io.github.loncra.basic.service.ai.server.service.agent;


import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEndEvent;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ConfirmResult;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.ToolCallState;
import io.agentscope.harness.agent.HarnessAgent;
import io.github.loncra.basic.service.ai.server.config.StreamConfig;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatBasicResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentResumeRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentPluginInstructionContext;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentStatusContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.CustomizeMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ToolCallBlockContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.*;
import io.github.loncra.basic.service.ai.server.interceptor.AgentStreamEventInterceptor;
import io.github.loncra.basic.service.ai.server.resolver.AgentChannelResolver;
import io.github.loncra.basic.service.ai.server.resolver.AgentEventResolver;
import io.github.loncra.basic.service.ai.server.resolver.AgentPluginMessageContentResolver;
import io.github.loncra.basic.service.ai.server.resolver.AgentSseStreamPublishResolver;
import io.github.loncra.basic.service.ai.server.resolver.event.AbstractAgentEventResolver;
import io.github.loncra.basic.service.ai.server.utils.ReactorContextUtils;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.chat.InstructionMessageMetadata;
import io.github.loncra.basic.service.commons.domain.metadata.chat.TextMessageMetadata;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.idempotent.annotation.Concurrent;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentManager {


    public final static String CONCURRENT_PREFIX = "loncra:basic-service:ai:app:agent:chat:concurrent:";

    private final AgentSseStreamPublishResolver agentSseStreamPublishResolver;

    private final List<AgentEventResolver> agentEventResolvers;

    private final List<AgentStreamEventInterceptor> agentStreamEventInterceptors;

    private final StreamConfig streamConfig;

    private final List<AgentPluginMessageContentResolver> pluginMessageContentResolvers;

    private final List<AgentChannelResolver> channelResolvers;

    @Concurrent(
            value = CONCURRENT_PREFIX + "[#body.channel:#body.agentConversationId]",
            condition = "[#body.agentConversationId != null]"
    )
    @Transactional(rollbackFor = Exception.class)
    public AgentChatResponseBody chat(
            AgentChatRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        return channelResolver(body.getChannel())
                .chat(body, token, deviceIdentified);
    }

    public AgentChannelResolver channelResolver(ExecutionChannelEnum channel) {
        return AgentChannelResolver.channelResolver(channelResolvers, channel);
    }

    public Flux<ServerSentEvent<String>> stream(
            Long assistantMessageId,
            ExecutionChannelEnum channel,
            boolean loadHistory,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        AgentMessageEntity assistant = Objects.requireNonNull(
                channelResolver(channel).getMessage(assistantMessageId, deviceIdentified),
                "执行 [" + assistantMessageId + "] 不存在或已结束"
        );
        SystemException.isTrue(AgentMessageRoleEnum.ASSISTANT.equals(assistant.getRole()), "ID 为 [" + assistantMessageId + "] 的消息记录非助手消息");
        PrincipalDetailsConstants.equals(assistant, token);
        if (AgentChatStatusEnum.COMPLETED.equals(assistant.getStatus())) {
            return Flux.fromIterable(agentSseStreamPublishResolver.getAgentMessageServerSentEvent(assistant));
        }
        return agentSseStreamPublishResolver.open(assistant, loadHistory);
    }

    public Page<AgentMessageEntity> histories(
            PageRequest request,
            MultiValueMap<String, Object> filter,
            ExecutionChannelEnum channel,
            Long conversationId,
            boolean totalPage,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        return channelResolver(channel).histories(request, filter, conversationId, totalPage, token, deviceIdentified);
    }

    public int positioningMessagePageNumber(
            ExecutionChannelEnum channel,
            Long conversationId,
            Long messageId,
            int pageSize,
            String deviceIdentified
    ) {
        return channelResolver(channel).positioningMessagePageNumber(conversationId, messageId, pageSize, deviceIdentified);
    }

    @Transactional(rollbackFor = Exception.class)
    public void execute(AgentExecutionMetadata execution) {
        AgentMessageEntity assistant = execution.getAssistantMessage();
        ExecutionChannelEnum channel = execution.getChannel();
        Flux<AbstractAssistantMessageContentMetadata> flux;
        if (AgentChatStatusEnum.REQUEST_STOP.equals(assistant.getStatus())) {
            List<ConfirmResult> confirmResults = assistant.obtainUserConfirmResultMetadataThenRemove();
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put(Msg.METADATA_CONFIRM_RESULTS, confirmResults);
            Msg msg = Msg.builderForRole(MsgRole.USER)
                    .metadata(metadata)
                    .build();
            channelResolver(channel).saveAssistant(execution, assistant);
            flux = Flux.using(
                    () -> channelResolver(channel).createHarnessAgent(execution),
                    agent -> executeAgentStreamEvents(agent, List.of(msg), execution),
                    HarnessAgent::close
            );
        } else {
            AgentMessageEntity userMessage = Objects.requireNonNull(
                    execution.getUserMessage(),
                    "找不到 ID 为 [" + assistant.getParentId() + "] 的用户消息记录"
            );
            List<AgentMessageEntity> assistantMessages = channelResolver(channel).findRequestStopAssistantMessage(
                    assistant.getAgentConversationId(),
                    execution.getDeviceIdentified()
            );
            if (CollectionUtils.isNotEmpty(assistantMessages)) {
                flux = Flux.empty();
                for (AgentMessageEntity rejectAssistantMessage : assistantMessages) {
                    List<ConfirmResult> confirmResults = createRejectConfirmResults(execution, rejectAssistantMessage);
                    if (CollectionUtils.isEmpty(confirmResults)) {
                        continue;
                    }
                    Msg confirmMsg = Msg.builder()
                            .role(MsgRole.USER)
                            .metadata(Map.of(Msg.METADATA_CONFIRM_RESULTS, confirmResults))
                            .build();
                    AgentExecutionMetadata rejectExecution = rejectExecution(execution, rejectAssistantMessage);
                    Flux<AbstractAssistantMessageContentMetadata> thenMany = Flux.using(
                            () -> channelResolver(channel).createHarnessAgent(rejectExecution),
                            agent -> executeAgentStreamEvents(agent, List.of(confirmMsg), rejectExecution),
                            HarnessAgent::close
                    );
                    flux = flux.thenMany(Flux.deferContextual(
                            ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> thenMany)
                    ));
                }
                Flux<AbstractAssistantMessageContentMetadata> thenMany = Flux.using(
                        () -> channelResolver(channel).createHarnessAgent(execution),
                        agent -> executeAgentStreamEvents(
                                agent,
                                List.of(createUserMentionMsg(agent, execution, userMessage)),
                                execution
                        ),
                        HarnessAgent::close
                );
                flux = flux.thenMany(Flux.deferContextual(
                        ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> thenMany)
                ));
            } else {
                flux = Flux.using(
                        () -> channelResolver(channel).createHarnessAgent(execution),
                        agent -> executeAgentStreamEvents(
                                agent,
                                List.of(createUserMentionMsg(agent, execution, userMessage)),
                                execution
                        ),
                        HarnessAgent::close
                );
            }
        }

        ReactorContextUtils.captureContext(flux)
                .doFinally(signal ->
                        channelResolver(channel).deleteConversationData(
                                assistant.getAgentConversationId(),
                                execution.getDeviceIdentified()
                        )
                )
                .subscribe(
                        s -> {},
                        error -> onError(error, execution)
                                .doOnNext(s -> publishStreamEvent(s, assistant, null))
                                .subscribe()
                );
    }

    private AgentExecutionMetadata rejectExecution(AgentExecutionMetadata execution, AgentMessageEntity rejectAssistantMessage) {
        AgentExecutionMetadata rejectExecution = new AgentExecutionMetadata();
        rejectExecution.setChannel(execution.getChannel());
        rejectExecution.setDeviceIdentified(execution.getDeviceIdentified());
        rejectExecution.setClientConversationId(execution.getClientConversationId());
        rejectExecution.setConversation(execution.getConversation());
        rejectExecution.setUserMessage(resolveUserMessage(execution, rejectAssistantMessage));
        rejectExecution.setAssistantMessage(rejectAssistantMessage);
        return rejectExecution;
    }

    private AgentMessageEntity resolveUserMessage(AgentExecutionMetadata execution, AgentMessageEntity assistantMessage) {
        if (Objects.equals(execution.getAssistantMessage().getParentId(), assistantMessage.getParentId())) {
            return execution.getUserMessage();
        }
        return channelResolver(execution.getChannel()).getMessage(
                assistantMessage.getParentId(),
                execution.getDeviceIdentified()
        );
    }

    private List<ConfirmResult> createRejectConfirmResults(AgentExecutionMetadata execution, AgentMessageEntity assistantMessage) {
        List<ConfirmResult> confirmResults = assistantMessage.obtainMessageContents()
                .stream()
                .filter(s -> AgentMessageContentTypeEnum.TOOL_CALL.getValue().equals(s.getType()))
                .map(ToolCallBlockContentMetadata.class::cast)
                .filter(s -> ToolCallState.PENDING.equals(s.getHitlStatus()))
                .map(ToolCallBlockContentMetadata::toToolUseBlock)
                .map(s -> new ConfirmResult(false, s))
                .toList();

        assistantMessage.setStatus(AgentChatStatusEnum.REJECT_ALL);
        channelResolver(execution.getChannel()).saveAssistant(execution, assistantMessage);

        return confirmResults;
    }

    private Flux<AbstractAssistantMessageContentMetadata> executeAgentStreamEvents(
            HarnessAgent agent,
            List<Msg> messages,
            AgentExecutionMetadata execution
    ) {
        AgentMessageEntity assistantMessage = execution.getAssistantMessage();
        ExecutionChannelEnum channel = execution.getChannel();

        if (!AgentChatStatusEnum.REJECT_ALL.equals(assistantMessage.getStatus())) {
            assistantMessage.setStatus(AgentChatStatusEnum.RUNNING);
            channelResolver(channel).saveAssistant(execution, assistantMessage);
        }
        channelResolver(channel).saveConversationStatus(execution, AgentChatStatusEnum.RUNNING);

        RuntimeContext context = RuntimeContext.builder()
                .userId(assistantMessage.obtainUserId())
                .sessionId(String.valueOf(assistantMessage.getAgentConversationId()))
                .put(SecurityContext.class, SecurityContextHolder.getContext())
                .put(AgentMessageRoleEnum.ASSISTANT.toString(), assistantMessage)
                .put(AgentExecutionMetadata.class, execution)
                .build();

        return agent.streamEvents(messages, context)
                .concatMap(e -> Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> convertEventToMessageContent(e, context))))
                .filter(Objects::nonNull)
                .onErrorResume(t -> Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> onError(t, context.get(AgentExecutionMetadata.class)))))
                .doOnNext(s -> publishStreamEvent(s, context));

    }

    private Msg createUserMentionMsg(
            HarnessAgent agent,
            AgentExecutionMetadata execution,
            AgentMessageEntity userMessage
    ) {
        AgentMessageEntity assistant = execution.getAssistantMessage();
        AgentConversationEntity conversation = channelResolver(execution.getChannel()).getConversation(
                assistant.getAgentConversationId(),
                execution.getDeviceIdentified()
        );
        AgentPluginInstructionContext context = new AgentPluginInstructionContext();
        context.setToolkit(agent.getToolkit());
        context.setAgent(agent);
        context.setUserMessage(userMessage);
        context.setWorkspaceId(resolveWorkspaceId(conversation));
        applyPluginInstructions(context);
        String prompt = TextMessageMetadata.ofString(userMessage.getContent());
        if (CollectionUtils.isNotEmpty(context.getMentionSummaries())) {
            prompt = prompt + "\n\n" + String.join("\n", context.getMentionSummaries());
        }
        return Msg.builder()
                .role(MsgRole.USER)
                .textContent(prompt)
                .build();
    }

    private void applyPluginInstructions(AgentPluginInstructionContext context) {
        for (AgentPluginMessageContentResolver resolver : pluginMessageContentResolvers) {
            List<InstructionMessageMetadata> hits = resolver.getHits(context.getUserMessage());
            if (CollectionUtils.isNotEmpty(hits)) {
                resolver.apply(context, hits);
            }
        }
    }

    private Long resolveWorkspaceId(AgentConversationEntity conversation) {
        if (conversation == null) {
            return null;
        }
        if (AgentConversationTypeEnum.WORKSPACE_CONVERSATION.equals(conversation.getType())) {
            return conversation.getParentId();
        }
        return conversation.getId();
    }

    private void publishStreamEvent(
            AbstractAssistantMessageContentMetadata message,
            RuntimeContext context
    ) {

        AgentMessageEntity assistant = context.get(AgentMessageRoleEnum.ASSISTANT.toString());
        publishStreamEvent(message, assistant, context);
    }

    private void publishStreamEvent(
            AbstractAssistantMessageContentMetadata message,
            AgentMessageEntity assistant,
            RuntimeContext context
    ) {
        agentSseStreamPublishResolver.publish(assistant.getAgentConversationId().toString(), message);

        if (Objects.nonNull(message.getEventSource())) {
            agentEventResolvers.stream()
                    .filter(s -> s.isSupport(message.getEventSource()))
                    .filter(s -> AbstractAgentEventResolver.class.isAssignableFrom(s.getClass()))
                    .map(s -> CastUtils.cast(s, AbstractAgentEventResolver.class))
                    .forEach(resolver -> resolverPostPublish(CastUtils.cast(resolver), message, assistant, context));
        }
    }

    private void resolverPostPublish(
            AbstractAgentEventResolver<AbstractAssistantMessageContentMetadata> resolver,
            AbstractAssistantMessageContentMetadata message,
            AgentMessageEntity assistant,
            RuntimeContext context
    ) {
        boolean cleanStream = resolver.postPublish(message, assistant, context);
        if (!cleanStream) {
            return ;
        }
        TimeProperties timeProperties = streamConfig.getDelayedExecutorCleanTime();
        if (Objects.nonNull(timeProperties)) {
            Executor executor = CompletableFuture.delayedExecutor(
                    streamConfig.getDelayedExecutorCleanTime().getValue(),
                    streamConfig.getDelayedExecutorCleanTime().getUnit()
            );
            CompletableFuture.runAsync(() -> agentSseStreamPublishResolver.clean(assistant.getAgentConversationId().toString(), message.getSseEventId()), executor);
        } else {
            agentSseStreamPublishResolver.clean(assistant.getAgentConversationId().toString(), message.getSseEventId());
        }
    }

    private Flux<AbstractAssistantMessageContentMetadata> onCompleted(
            AgentExecutionMetadata execution
    ) {
        AgentMessageEntity assistant = execution.getAssistantMessage();
        AgentConversationEntity conversation = channelResolver(execution.getChannel()).getConversation(
                assistant.getAgentConversationId(),
                execution.getDeviceIdentified()
        );
        if (Objects.isNull(conversation) || !AgentChatStatusEnum.STREAM_END_STATUS.contains(conversation.getStatus())) {
            log.warn("会话 [{}] 当前未处于完成状态略过推送 STREAM_END 事件", assistant.getId());
            return Flux.empty();
        }

        CustomizeMetadata content = new CustomizeMetadata();
        content.setEventType(AgentMessageContentTypeEnum.STREAM_END);
        content.setId(assistant.getAgentConversationId().toString());
        content.setAssistantMessageId(assistant.getId());
        content.getMetadata().put(SystemConstants.STATUS_TABLE_FIELD_NAME, assistant.getStatus());

        if (log.isDebugEnabled()) {
            log.debug("助手消息 [{}] 执行完成, 推送 STREAM_END 事件", assistant.getId());
        }

        return Flux.just(content);
    }

    public Flux<AbstractAssistantMessageContentMetadata> onError(
            Throwable t,
            AgentExecutionMetadata execution
    ) {
        AgentMessageEntity assistant = execution.getAssistantMessage();
        log.error("助手消息 [{}] 执行失败", assistant.getId(), t);

        CustomizeMetadata content = new CustomizeMetadata();
        content.setEventType(AgentMessageContentTypeEnum.ERROR);
        content.setId(assistant.getId().toString());
        content.getMetadata().put(RestResult.DEFAULT_MESSAGE_NAME, t.getMessage());
        assistant.updateContent(content);
        assistant.setStatus(AgentChatStatusEnum.FAILED);
        channelResolver(execution.getChannel()).saveAssistant(execution, assistant);

        AgentStatusContentMetadata agentEndContent = new AgentStatusContentMetadata();
        agentEndContent.setId(assistant.getAgentConversationId().toString());
        agentEndContent.setStatus(AgentChatStatusEnum.FAILED);

        channelResolver(execution.getChannel()).saveConversationStatus(execution, AgentChatStatusEnum.FAILED);
        return Flux.just(agentEndContent, content).concatWith(onCompleted(execution));
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
            RuntimeContext context
    ) {

        AgentMessageEntity assistant = context.get(AgentMessageRoleEnum.ASSISTANT.toString());
        if (log.isDebugEnabled()) {
            log.debug("助手消息 [{}] 收到事件 {}", assistant.getId(), CastUtils.convertValue(event, CastUtils.MAP_TYPE_REFERENCE));
        }

        List<AbstractAssistantMessageContentMetadata> contents = agentEventResolvers.stream()
                .filter(s -> s.isSupport(event))
                .flatMap(s -> s.process(assistant, event, context).stream())
                .toList();
        Flux<AbstractAssistantMessageContentMetadata> flux = Flux.fromIterable(contents);
        if (AgentEndEvent.class.isAssignableFrom(event.getClass())) {
            flux = flux.concatWith(Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> postStreamEvent(assistant))))
                    .concatWith(Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> onCompleted(context.get(AgentExecutionMetadata.class)))));
        }
        return flux;
    }

    @Transactional(rollbackFor = Exception.class)
    public AgentChatBasicResponseBody resume(
            AgentResumeRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        return channelResolver(body.getChannel())
                .resume(body, token, deviceIdentified);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(
            List<Integer> ids,
            ExecutionChannelEnum channel,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        channelResolver(channel).deleteConversation(ids, token, deviceIdentified);
    }

    public Long interrupt(
            Long assistantMessageId,
            ExecutionChannelEnum channel,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        AgentMessageEntity assistant = Objects.requireNonNull(
                channelResolver(channel).getMessage(assistantMessageId, deviceIdentified),
                "执行 [" + assistantMessageId + "] 不存在或已结束"
        );
        PrincipalDetailsConstants.equals(assistant, token);
        SystemException.isTrue(
                !AgentChatStatusEnum.COMPLETED_STATUS.contains(assistant.getStatus()),
                "当前助手消息已结束，无法停止"
        );

        agentSseStreamPublishResolver.interrupt(assistant);

        return assistant.getAgentConversationId();
    }
}
