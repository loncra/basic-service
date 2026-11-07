package io.github.loncra.basic.service.ai.server.consumer;

import com.rabbitmq.client.Channel;
import io.github.loncra.basic.service.ai.api.constants.AiConstants;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecuteMessageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentChannelResolver;
import io.github.loncra.basic.service.ai.server.service.agent.AgentManager;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentConsumer {

    private final AgentManager agentManager;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = AiConstants.MQ_AGENT_EXECUTE_QUEUE, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_AI_RABBITMQ_EXCHANGE),
                    key = AiConstants.MQ_AGENT_EXECUTE_QUEUE
            )
    )
    public void execute(
            String payload,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag
    ) throws IOException {
        AgentExecuteMessageMetadata executeMessage;
        try {
            executeMessage = CastUtils.getObjectMapper().readValue(payload, AgentExecuteMessageMetadata.class);
        }
        catch (Exception e) {
            log.error("解析执行消息失败", e);
            channel.basicNack(tag, false, false);
            return;
        }

        AgentExecutionMetadata execution;
        try {
            AgentChannelResolver resolver = AgentChannelResolver.channelResolver(agentManager.getChannelResolvers(), executeMessage.getChannel());
            execution = resolver.getAgentExecutionMetadata(executeMessage);
        }
        catch (Exception e) {
            log.info("执行引用 [{}] 组装执行内容失败，跳过执行: {}", executeMessage.getRef(), e.getMessage());
            channel.basicAck(tag, false);
            return;
        }

        AgentMessageEntity assistant = Objects.nonNull(execution) ? execution.getAssistantMessage() : null;
        if (Objects.isNull(assistant)) {
            log.info("找不到 ID 为 [{}] 的助手消息", executeMessage.getRef());
            channel.basicNack(tag, false, false);
            return;
        }
        if (assistant.getRole() != AgentMessageRoleEnum.ASSISTANT) {
            log.info("ID 为 [{}] 的消息非助手消息", assistant.getId());
            channel.basicNack(tag, false, false);
            return;
        }
        if (AgentChatStatusEnum.COMPLETED_STATUS.contains(assistant.getStatus())) {
            log.info("ID 为 [{}] 的助手消息已终态 [{}]，跳过执行", assistant.getId(), assistant.getStatus());
            channel.basicNack(tag, false, false);
            return;
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (Objects.isNull(securityContext.getAuthentication()) || !AuditAuthenticationToken.class.isAssignableFrom(securityContext.getAuthentication().getClass())) {
            log.info("ID 为 [{}] 的助手消息在本次消费中无法获取认证信息跳过执行", assistant.getId());
            channel.basicNack(tag, false, false);
            return;
        }
        execution.setToken(CastUtils.cast(securityContext.getAuthentication()));

        try {
            agentManager.execute(execution);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("执行 ID 为 [{}] 的助手消息失败", assistant.getId(), e);
            channel.basicNack(tag, false, false);
        }
    }
}
