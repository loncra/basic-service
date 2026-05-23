package io.github.loncra.basic.service.auth.server.consumer;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import io.github.loncra.basic.service.auth.server.domain.entity.user.ConsoleUserEntity;
import io.github.loncra.basic.service.auth.server.service.user.console.ConsoleUserService;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.ExportDataMetadata;
import io.github.loncra.basic.service.resource.api.service.AttachmentServiceClient;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.HttpRequestParameterMapUtils;
import io.github.loncra.framework.commons.MetadataUtils;
import io.github.loncra.framework.commons.domain.metadata.TreeDescriptionMetadata;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsoleUserConsumer {

    private final ConsoleUserService consoleUserService;

    private final AttachmentServiceClient attachmentServiceClient;

    public static final String DEFAULT_EXPORT_QUEUE_NAME = "auth.server.console.user.export";

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_EXPORT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_AUTH_RABBITMQ_EXCHANGE),
                    key = DEFAULT_EXPORT_QUEUE_NAME
            )
    )
    @Transactional(rollbackFor = Exception.class)
    public void export(@Payload String cacheName,
                       Channel channel,
                       @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        RBucket<ExportDataMetadata> bucket = consoleUserService.getRedissonClient().getBucket(SystemConstants.USER_EXPORT_CACHE.getName(cacheName));
        if (!bucket.isExists()) {
            log.info("[后台用户导出]: 找不到缓存为 {} 的桶信息", cacheName);
            channel.basicNack(tag, false, false);
            return ;
        }

        ExportDataMetadata exportDataMetadata = bucket.get();
        exportDataMetadata.setExpiresTime(exportDataMetadata.getCreationTime().plus(SystemConstants.USER_EXPORT_CACHE.getExpiresTime().toDuration()));
        Map<String, String[]> queryParams = CastUtils.cast(exportDataMetadata.getMetadata().get(SystemConstants.QUERY_KEY));
        MultiValueMap<String, String> param = HttpRequestParameterMapUtils.castMapToMultiValueMap(queryParams);
        QueryWrapper<ConsoleUserEntity> query = consoleUserService.getQueryGenerator()
                .createQueryWrapperFromMap(new LinkedHashMap<>(param));
        List<ConsoleUserEntity> data = consoleUserService.find(query);

        exportDataMetadata.setExecuteStatus(ExecuteStatus.Processing);
        bucket.set(exportDataMetadata, Duration.between(Instant.EPOCH, exportDataMetadata.getExpiresTime()));
        TreeDescriptionMetadata descriptionMetadata = MetadataUtils.convertDescriptionMetadata(ConsoleUserEntity.class);
        attachmentServiceClient.export(descriptionMetadata, new LinkedList<>(data), exportDataMetadata, IdValueMetadata::getValue);
        exportDataMetadata.setSuccessTime(Instant.now());
        bucket.set(exportDataMetadata, Duration.between(Instant.EPOCH, exportDataMetadata.getExpiresTime()));

        channel.basicNack(tag, false, false);
    }
}
