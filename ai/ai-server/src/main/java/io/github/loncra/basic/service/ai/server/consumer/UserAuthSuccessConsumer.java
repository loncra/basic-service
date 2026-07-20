package io.github.loncra.basic.service.ai.server.consumer;

import com.rabbitmq.client.Channel;
import io.github.loncra.basic.service.ai.api.constants.AiMqConstants;
import io.github.loncra.basic.service.ai.server.service.agent.AgentConversationService;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserAuthSuccessConsumer {

    private final AgentConversationService agentConversationService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = AiMqConstants.USER_AUTH_SUCCESS_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(
                            value = SystemConstants.USER_AUTH_SUCCESS_FANOUT_EXCHANGE,
                            type = ExchangeTypes.FANOUT
                    ),
                    key = AiMqConstants.USER_AUTH_SUCCESS_QUEUE_NAME
            )
    )
    public void onMessage(@Payload String principal,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        if (Objects.isNull(SecurityContextHolder.getContext()) || Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
            channel.basicNack(tag, false, false);
            return ;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!AuditAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            channel.basicNack(tag, false, false);
            return ;
        }

        AuditAuthenticationToken token = CastUtils.cast(SecurityContextHolder.getContext().getAuthentication());

        agentConversationService.createDefaultIfNotExist(token);

        channel.basicAck(tag, false);
    }
}
