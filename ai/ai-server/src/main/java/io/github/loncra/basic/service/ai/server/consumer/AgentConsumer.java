package io.github.loncra.basic.service.ai.server.consumer;

import com.rabbitmq.client.Channel;
import io.github.loncra.basic.service.ai.api.constants.AiConstants;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecuteMessageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.ai.server.service.agent.AgentExecutionStore;
import io.github.loncra.basic.service.ai.server.service.agent.AgentManager;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentConsumer {

    private final AgentManager agentManager;

    private final AgentExecutionStore agentExecutionStore;

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

        AgentMessageEntity assistant;
        if (ExecutionChannelEnum.MYSQL.equals(executeMessage.getChannel())) {
            assistant = agentManager.getMessageService().get(Long.valueOf(executeMessage.getRef()));
        } else {
            AgentExecutionMetadata execution = agentExecutionStore.get(executeMessage.getRef());
            if (Objects.isNull(execution)) {
                log.info("执行引用 [{}] 不存在或已过期，跳过执行", executeMessage.getRef());
                channel.basicAck(tag, false);
                return;
            }
            assistant = execution.getAssistantMessage();
        }

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

        assistant.setExecutionChannel(executeMessage.getChannel());
        assistant.setExecutionRef(executeMessage.getRef());
        assistant.setDeviceIdentified(executeMessage.getDeviceIdentified());

        try {
            agentManager.execute(assistant);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("执行 ID 为 [{}] 的助手消息失败", assistant.getId(), e);
            channel.basicNack(tag, false, false);
        }
    }
}
