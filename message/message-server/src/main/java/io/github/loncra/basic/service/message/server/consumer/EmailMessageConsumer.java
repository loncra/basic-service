package io.github.loncra.basic.service.message.server.consumer;

import com.rabbitmq.client.Channel;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.message.server.constants.MessageMqConstants;
import io.github.loncra.basic.service.message.server.resolver.support.EmailMessageSenderResolver;
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

/**
 * 邮件消息 MQ 消费者
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailMessageConsumer {

    private final EmailMessageSenderResolver emailMessageSenderResolver;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = MessageMqConstants.EMAIL_QUEUE, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_MESSAGE_RABBITMQ_EXCHANGE),
                    key = MessageMqConstants.EMAIL_QUEUE
            )
    )
    public void onMessage(Long id,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        emailMessageSenderResolver.sendMessage(id);
        channel.basicAck(tag, false);
    }
}
