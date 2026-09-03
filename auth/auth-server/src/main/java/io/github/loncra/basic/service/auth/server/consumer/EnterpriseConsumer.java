package io.github.loncra.basic.service.auth.server.consumer;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import io.github.loncra.basic.service.auth.api.constants.AuthenticationMqConstants;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseEntity;
import io.github.loncra.basic.service.auth.server.service.enterprise.EnterpriseService;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.ExportDataMetadata;
import io.github.loncra.basic.service.resource.api.service.AttachmentServiceClient;
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

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnterpriseConsumer {

    private final EnterpriseService enterpriseService;

    private final AttachmentServiceClient attachmentServiceClient;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            value = AuthenticationMqConstants.ENTERPRISE_EXPORT_QUEUE_NAME,
                            durable = "true"
                    ),
                    exchange = @Exchange(value = SystemConstants.SYS_AUTH_RABBITMQ_EXCHANGE),
                    key = AuthenticationMqConstants.ENTERPRISE_EXPORT_QUEUE_NAME
            )
    )
    @Transactional(rollbackFor = Exception.class)
    public void export(@Payload String cacheName,
                       Channel channel,
                       @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        RBucket<ExportDataMetadata> bucket = enterpriseService.getRedissonClient().getBucket(SystemConstants.USER_EXPORT_CACHE.getName(cacheName));
        if (!bucket.isExists()) {
            log.info("[企业导出]: 找不到缓存为 {} 的桶信息", cacheName);
            channel.basicNack(tag, false, false);
            return ;
        }

        ExportDataMetadata exportDataMetadata = bucket.get();
        exportDataMetadata.setExpiresTime(exportDataMetadata.getCreationTime().plus(SystemConstants.USER_EXPORT_CACHE.getExpiresTime().toDuration()));
        QueryWrapper<EnterpriseEntity> query = enterpriseService.getQueryGenerator()
                .createQueryWrapperFromMap(exportDataMetadata.getQueryMap());
        List<EnterpriseEntity> data = enterpriseService.find(query);

        exportDataMetadata.setExecuteStatus(ExecuteStatus.Processing);
        bucket.set(exportDataMetadata, Duration.between(Instant.EPOCH, exportDataMetadata.getExpiresTime()));
        TreeDescriptionMetadata descriptionMetadata = MetadataUtils.convertDescriptionMetadata(EnterpriseEntity.class);
        attachmentServiceClient.export(descriptionMetadata, new LinkedList<>(data), exportDataMetadata, IdValueMetadata::getValue);
        exportDataMetadata.setSuccessTime(Instant.now());
        bucket.set(exportDataMetadata, Duration.between(Instant.EPOCH, exportDataMetadata.getExpiresTime()));

        channel.basicAck(tag, false);
    }
}
