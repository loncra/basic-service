package io.github.loncra.basic.service.ai.server.consumer;

import com.rabbitmq.client.Channel;
import io.github.loncra.basic.service.ai.api.constants.AiConstants;
import io.github.loncra.basic.service.ai.server.service.agent.AgentConversationService;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.tenant.SimpleTenantContext;
import io.github.loncra.framework.commons.tenant.holder.TenantContextHolder;
import io.github.loncra.framework.spring.security.core.authentication.TenantContextSecurityFilter;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthSuccessConsumer {

    private final AgentConversationService agentConversationService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = AiConstants.MQ_USER_AUTH_SUCCESS_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(
                            value = SystemConstants.USER_AUTH_SUCCESS_FANOUT_EXCHANGE,
                            type = ExchangeTypes.FANOUT
                    ),
                    key = AiConstants.MQ_USER_AUTH_SUCCESS_QUEUE_NAME
            )
    )
    public void onMessage(Channel channel,
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

        SimpleTenantContext tenantContext = TenantContextSecurityFilter.resolveTenantContext(SecurityContextHolder.getContext());
        TenantContextHolder.set(tenantContext);

        AuditAuthenticationToken token = CastUtils.cast(SecurityContextHolder.getContext().getAuthentication());

        try {
            agentConversationService.createDefaultIfNotExist(token);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("初始化默认工作空间失败, 消息将丢弃", e);
            channel.basicNack(tag, false, false);   // requeue=false，或转发死信
        }
    }
}
