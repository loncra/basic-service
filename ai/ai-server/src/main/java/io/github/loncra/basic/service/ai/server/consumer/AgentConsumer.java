package io.github.loncra.basic.service.ai.server.consumer;

import com.rabbitmq.client.Channel;
import io.github.loncra.basic.service.ai.api.constants.AiMqConstants;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.basic.service.ai.server.service.agent.AgentManager;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
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

/**
 * 助手消息异步生成消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentConsumer {

    private final AgentManager agentManager;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = AiMqConstants.AGENT_STREAM_QUEUE, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_AI_RABBITMQ_EXCHANGE),
                    key = AiMqConstants.AGENT_STREAM_QUEUE
            )
    )
    public void stream(
            Long assistantId,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag
    ) throws IOException {
        AgentMessageEntity assistant = agentManager.getMessageService().get(assistantId);
        if (Objects.isNull(assistant)) {
            log.info("找不到 ID 为 [{}] 的助手消息", assistantId);
            channel.basicNack(tag, false, false);
            return ;
        }
        if (assistant.getRole() != AgentMessageRoleEnum.ASSISTANT) {
            log.info("ID 为 [{}] 的消息非助手消息", assistantId);
            channel.basicNack(tag, false, false);
            return ;
        }
        if (AgentChatStatusEnum.COMPLETED_STATUS.contains(assistant.getStatus())) {
            log.info("ID 为 [{}] 的助手消息已终态 [{}]，跳过执行", assistantId, assistant.getStatus());
            channel.basicNack(tag, false, false);
            return;
        }
        try {
            agentManager.execute(assistant);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            channel.basicNack(tag, false, false);
        }
    }
}
