package io.github.loncra.basic.service.auth.server.consumer;

import com.rabbitmq.client.Channel;
import io.github.loncra.basic.service.auth.server.domain.dto.ScanSyncPluginResourceDto;
import io.github.loncra.basic.service.auth.server.resolver.PluginResourceResolver;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PluginResourceConumer {

    public static final String DEFAULT_QUEUE_NAME = "auth.server.plugin.resource";

    private final List<PluginResourceResolver> pluginResourceResolver;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_AUTH_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void onMessage(
            @Payload
            String json,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG)
            long tag
    ) throws IOException {

        try {
            ScanSyncPluginResourceDto dto = CastUtils.getObjectMapper().readValue(json, ScanSyncPluginResourceDto.class);
            if (CollectionUtils.isNotEmpty(pluginResourceResolver) && CollectionUtils.isNotEmpty(dto.getResources())) {
                pluginResourceResolver.forEach(i -> i.postSyncPlugin(dto));
            }
        } catch (Exception e) {
            log.warn("执行插件资源解析器出现异常", e);
        }

        channel.basicAck(tag, false);
    }
}
