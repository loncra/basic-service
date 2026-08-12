package io.github.loncra.basic.service.ai.server.service.agent;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEndEvent;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ConfirmResult;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.ToolCallState;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.skill.repository.FileSystemSkillRepository;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.github.loncra.basic.service.ai.api.constants.AiConstants;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.config.SkillConfig;
import io.github.loncra.basic.service.ai.server.config.StreamConfig;
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
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.TimeProperties;
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
import org.apache.commons.lang3.Strings;
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
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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

    private final SkillConfig skillConfig;

    private final AgentStateStore agentStateStore;

    private final StreamConfig streamConfig;

    private final List<MiddlewareBase> middlewares;

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

        ModelSettingEntity model = Objects.requireNonNull(modelSettingService.get(body.getModelId()), "找不到 ID 为 [" + body.getModelId() + "] 的模型数据");
        ModelSettingMetadata modelMetadata = CastUtils.of(model, ModelSettingMetadata.class);

        if (conversation.getType() != AgentConversationTypeEnum.WORKSPACE_CONVERSATION) {
            Long parentId = conversation.getId();

            conversation = new AgentConversationEntity();
            conversation.setType(AgentConversationTypeEnum.WORKSPACE_CONVERSATION);
            conversation.setParentId(parentId);
            conversation.setPrincipal(token.getName());
        } else {
            assertNoRunningAssistant(conversation.getId());
        }

        conversation.setStatus(AgentChatStatusEnum.READY);
        conversation.setLastModel(modelMetadata);
        conversation.setLastChatType(body.getType());

        conversationService.save(conversation);

        AgentMessageEntity userMessage = CastUtils.of(body, AgentMessageEntity.class);
        userMessage.setModel(modelMetadata);
        userMessage.setRole(AgentMessageRoleEnum.USER);
        userMessage.setPrincipal(token.getName());
        userMessage.setStatus(AgentChatStatusEnum.READY);
        userMessage.setAgentConversationId(conversation.getId());
        messageService.insert(userMessage);

        AgentMessageEntity assistantMessage = CastUtils.of(userMessage, AgentMessageEntity.class, IdEntity.ID_FIELD_NAME);
        assistantMessage.setRole(AgentMessageRoleEnum.ASSISTANT);
        assistantMessage.setParentId(userMessage.getId());
        assistantMessage.setContent(new LinkedList<>());
        messageService.insert(assistantMessage);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                amqpTemplate.convertAndSend(
                        SystemConstants.SYS_AI_RABBITMQ_EXCHANGE,
                        AiConstants.MQ_AGENT_EXECUTE_QUEUE,
                        assistantMessage.getId()
                );
            }
        });

        AgentChatResponseBody responseBody = new AgentChatResponseBody();
        responseBody.setConversation(conversation);
        responseBody.setUserMessageId(userMessage.getId());
        responseBody.setAssistantMessageId(assistantMessage.getId());

        publishStreamStartSse(assistantMessage);

        return responseBody;
    }

    private void publishStreamStartSse(
            AgentMessageEntity assistantMessage
    ) {
        CustomizeMetadata metadata = new CustomizeMetadata();
        metadata.setId(assistantMessage.getId().toString());
        metadata.setEventType(AgentMessageContentTypeEnum.STREAM_START);

        agentSseStreamPublishResolver.publish(assistantMessage.getAgentConversationId().toString(), metadata);
    }

    public Flux<ServerSentEvent<String>> stream(
            Long assistantId,
            boolean loadHistory,
            AuditAuthenticationToken token
    ) {
        AgentMessageEntity assistant = messageService.get(assistantId);
        SystemException.isTrue(Objects.nonNull(assistant), "找不到 ID 为 [" + assistantId + "] 的助手消息");
        SystemException.isTrue(AgentMessageRoleEnum.ASSISTANT.equals(assistant.getRole()), "ID 为 [" + assistantId + "] 的消息记录非助手消息");
        PrincipalDetailsConstants.equals(assistant, token);
        if (AgentChatStatusEnum.COMPLETED.equals(assistant.getStatus())) {
            return Flux.fromIterable(agentSseStreamPublishResolver.getAgentMessageServerSentEvent(assistant));
        }
        return agentSseStreamPublishResolver.open(assistant, loadHistory);
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

    @Transactional(rollbackFor = Exception.class)
    public void execute(AgentMessageEntity assistant) {
        Flux<AbstractAssistantMessageContentMetadata> flux;
        if (AgentChatStatusEnum.REQUEST_STOP.equals(assistant.getStatus())) {
            List<ConfirmResult> confirmResults = assistant.obtainUserConfirmResultMetadataThenRemove();
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put(Msg.METADATA_CONFIRM_RESULTS, confirmResults);
            Msg msg = Msg.builderForRole(MsgRole.USER)
                    .metadata(metadata)
                    .build();
            messageService.lambdaUpdate()
                    .set(AgentChatMetadata::getMetadata, assistant.obtainMetadataJsonString())
                    .eq(AgentMessageEntity::getId, assistant.getId())
                    .update();
            flux = Flux.using(
                    () -> createHarnessAgent(assistant),
                    agent -> executeAgentStreamEvents(agent, List.of(msg), assistant),
                    HarnessAgent::close
            );
        } else {
            AgentMessageEntity userMessage = Objects.requireNonNull(messageService.get(assistant.getParentId()), "找不到 ID 为 [" + assistant.getParentId() + "] 的用户消息记录");
            String prompt = TextMessageMetadata.ofString(userMessage.getContent());
            Msg userMsg = Msg.builder().
                    role(MsgRole.USER)
                    .textContent(prompt)
                    .build();

            List<AgentMessageEntity> assistantMessages = messageService.findRequestStopAssistantMessage(assistant.getAgentConversationId());
            if (CollectionUtils.isNotEmpty(assistantMessages)) {
                flux = Flux.empty();
                for (AgentMessageEntity rejectAssistantMessage : assistantMessages) {
                    List<ConfirmResult> confirmResults = createRejectConfirmResults(rejectAssistantMessage);
                    if (CollectionUtils.isEmpty(confirmResults)) {
                        continue;
                    }
                    Msg confirmMsg = Msg.builder().role(MsgRole.USER).metadata(Map.of(Msg.METADATA_CONFIRM_RESULTS, confirmResults)).build();
                    Flux<AbstractAssistantMessageContentMetadata> thenMany = Flux.using(
                            () -> createHarnessAgent(rejectAssistantMessage),
                            agent -> executeAgentStreamEvents(agent, List.of(confirmMsg), rejectAssistantMessage),
                            HarnessAgent::close
                    );
                    flux = flux.thenMany(Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> thenMany)));
                }

                Flux<AbstractAssistantMessageContentMetadata> thenMany = Flux.using(
                        () -> createHarnessAgent(assistant),
                        agent -> executeAgentStreamEvents(agent, List.of(userMsg), assistant),
                        HarnessAgent::close
                );
                flux = flux.thenMany(Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> thenMany)));
            } else {
                flux = Flux.using(
                        () -> createHarnessAgent(assistant),
                        agent -> executeAgentStreamEvents(agent, List.of(userMsg), assistant),
                        HarnessAgent::close
                );
            }
        }

        ReactorContextUtils.captureContext(flux)
                .subscribe(
                        s -> {},
                        error -> onError(error, assistant)
                                .doOnNext(s -> publishStreamEvent(s, assistant))
                                .subscribe()
                );
    }

    private List<ConfirmResult> createRejectConfirmResults(AgentMessageEntity assistantMessage) {
        List<ConfirmResult> confirmResults = assistantMessage.obtainMessageContents()
                .stream()
                .filter(s -> AgentMessageContentTypeEnum.TOOL_CALL.getValue().equals(s.getType()))
                .map(ToolCallBlockContentMetadata.class::cast)
                .filter(s -> ToolCallState.PENDING.equals(s.getHitlStatus()))
                /*.peek(s -> s.setUserConfirmed(false))
                .peek(s -> s.setStatus(AgentBlockStatusEnum.DONE))
                .peek(assistantMessage::updateContent)*/
                .map(ToolCallBlockContentMetadata::toToolUseBlock)
                .map(s -> new ConfirmResult(false, s))
                .toList();

        assistantMessage.setStatus(AgentChatStatusEnum.REJECT_ALL);

        messageService.lambdaUpdate()
                .set(AgentMessageEntity::getStatus, AgentChatStatusEnum.REJECT_ALL.getValue())
                //.set(AgentChatMetadata::getContent, assistantMessage.obtainContentJsonString())
                .eq(AgentMessageEntity::getId, assistantMessage.getId())
                .update();

        return confirmResults;
    }

    private Flux<AbstractAssistantMessageContentMetadata> executeAgentStreamEvents(
            HarnessAgent agent,
            List<Msg> messages,
            AgentMessageEntity assistantMessage
    ) {

        if (!AgentChatStatusEnum.REJECT_ALL.equals(assistantMessage.getStatus())) {
            assistantMessage.setStatus(AgentChatStatusEnum.RUNNING);
            messageService.lambdaUpdate()
                    .set(AgentMessageEntity::getStatus, AgentChatStatusEnum.RUNNING.getValue())
                    .eq(AgentMessageEntity::getId, assistantMessage.getId())
                    .update();
        }
        conversationService.lambdaUpdate()
                .set(AgentConversationEntity::getStatus, AgentChatStatusEnum.RUNNING.getValue())
                .eq(AgentConversationEntity::getId, assistantMessage.getAgentConversationId())
                .update();

        RuntimeContext context = RuntimeContext.builder()
                .userId(assistantMessage.obtainUserId())
                .sessionId(String.valueOf(assistantMessage.getAgentConversationId()))
                .put(SecurityContext.class, SecurityContextHolder.getContext())
                .put(AgentMessageRoleEnum.ASSISTANT.toString(), assistantMessage)
                .build();

        return agent.streamEvents(messages, context)
                .concatMap(e -> Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> convertEventToMessageContent(e, context))))
                .filter(Objects::nonNull)
                .onErrorResume(t -> Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> onError(t, context))))
                .doOnNext(s -> publishStreamEvent(s, context));

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
                // 激活元工具，用于处理元工具调用，让 skill 动态加载工具
                .enableMetaTool(true)
                .stateStore(agentStateStore)
                .skillRepository(new FileSystemSkillRepository(Path.of(skillConfig.getPath())))
                .compaction(aiAppConfig.toCompactionConfig())
                .workspace(aiAppConfig.getWorkspacePath() + File.separator + workspace.getId())
                // 中断信号中间件实现
                .middlewares(middlewares);
        if (!AgentChatTypeEnum.ASK.equals(assistant.getType())) {
            builder.enablePlanMode()
                    .planFileDirectory(assistant.getType().toString());
        }
        return builder.build();
    }

    private void publishStreamEvent(
            AbstractAssistantMessageContentMetadata message,
            RuntimeContext context
    ) {

        AgentMessageEntity assistant = context.get(AgentMessageRoleEnum.ASSISTANT.toString());
        publishStreamEvent(message, assistant);
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
                    .forEach(resolver -> resolverPostPublish(CastUtils.cast(resolver), message, assistant));
        }
    }

    private void resolverPostPublish(
            AbstractAgentEventResolver<AbstractAssistantMessageContentMetadata> resolver,
            AbstractAssistantMessageContentMetadata message,
            AgentMessageEntity assistant
    ) {
        boolean cleanStream = resolver.postPublish(message, assistant);
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
            AgentMessageEntity assistant
    ) {
        AgentConversationEntity conversation = conversationService.get(assistant.getAgentConversationId());
        if (!AgentChatStatusEnum.STREAM_END_STATUS.contains(conversation.getStatus())) {
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
            RuntimeContext context
    ) {

        AgentMessageEntity assistant = context.get(AgentMessageRoleEnum.ASSISTANT.toString());
        return onError(t, assistant);
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
                    .concatWith(Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> onCompleted(assistant))));
        }
        return flux;
    }

    @Transactional(rollbackFor = Exception.class)
    public AgentChatBasicResponseBody resume(
            AgentResumeRequestBody body,
            AuditAuthenticationToken token
    ) {
        AgentMessageEntity assistantMessage = Objects.requireNonNull(messageService.get(body.getAssistantMessageId()));
        PrincipalDetailsConstants.equals(assistantMessage, token);

        List<ConfirmResult> confirmResults = getResumeConfirmResults(body, assistantMessage);

        assistantMessage.saveUserConfirmResultMetadata(confirmResults);

        messageService.lambdaUpdate()
                .set(AgentChatMetadata::getContent, assistantMessage.obtainContentJsonString())
                .set(AgentChatMetadata::getMetadata, assistantMessage.obtainMetadataJsonString())
                .eq(AgentMessageEntity::getId, assistantMessage.getId())
                .update();

        publishStreamStartSse(assistantMessage);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                amqpTemplate.convertAndSend(
                        SystemConstants.SYS_AI_RABBITMQ_EXCHANGE,
                        AiConstants.MQ_AGENT_EXECUTE_QUEUE,
                        assistantMessage.getId()
                );
            }
        });

        AgentChatBasicResponseBody result = new AgentChatBasicResponseBody();
        result.setAssistantMessageId(assistantMessage.getId());
        result.setUserMessageId(assistantMessage.getParentId());

        return result;
    }

    private static List<ConfirmResult> getResumeConfirmResults(
            AgentResumeRequestBody body,
            AgentMessageEntity assistantMessage
    ) {
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
                    .orElseThrow(() -> new SystemException("ID 为 [" + toolUseBlock.getId() + "] 的工具需要确认"));
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
                contentMetadata.setHitlStatus(ToolCallState.SUBMITTED);
                assistantMessage.updateContent(contentMetadata);
            }

        }
        return confirmResults;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(
            List<Integer> ids,
            AuditAuthenticationToken token
    ) {
        conversationService.get(ids).forEach(conversation -> deleteConversation(conversation, token));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(
            AgentConversationEntity entity,
            AuditAuthenticationToken token
    ) {
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

    public Long interrupt(
            Long assistantMessageId,
            AuditAuthenticationToken token
    ) {
        AgentMessageEntity assistant = Objects.requireNonNull(messageService.get(assistantMessageId), "找不到 ID 为 [" + assistantMessageId + "] 的助手信息");
        PrincipalDetailsConstants.equals(assistant, token);
        SystemException.isTrue(
                !AgentChatStatusEnum.COMPLETED_STATUS.contains(assistant.getStatus()),
                "当前助手消息已结束，无法停止"
        );

        agentSseStreamPublishResolver.interrupt(assistant);

        return assistant.getAgentConversationId();
    }
}
