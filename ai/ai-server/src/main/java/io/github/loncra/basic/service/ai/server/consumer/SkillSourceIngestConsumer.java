package io.github.loncra.basic.service.ai.server.consumer;

import com.rabbitmq.client.Channel;
import io.github.loncra.basic.service.ai.api.constants.AiConstants;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillPackageEntity;
import io.github.loncra.basic.service.ai.server.resolver.SkillSourceResolver;
import io.github.loncra.basic.service.ai.server.service.hub.AiSkillPackageService;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
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
import java.util.List;
import java.util.Objects;

/**
 * Skill 来源异步物化消费者。
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillSourceIngestConsumer {

    private final AiSkillPackageService aiSkillPackageService;

    private final List<SkillSourceResolver> skillSourceResolvers;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = AiConstants.MQ_SKILL_SOURCE_INGEST_QUEUE, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_AI_RABBITMQ_EXCHANGE),
                    key = AiConstants.MQ_SKILL_SOURCE_INGEST_QUEUE
            )
    )
    public void ingest(
            Long id,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag
    ) throws IOException {
        AiSkillPackageEntity entity = aiSkillPackageService.get(id);
        if (Objects.isNull(entity)) {
            log.info("找不到 ID 为 [{}] 的 Skill 目录", id);
            channel.basicNack(tag, false, false);
            return;
        }
        if (entity.getExecuteStatus() != ExecuteStatus.Pending) {
            log.info("ID 为 [{}] 的 Skill 目录执行状态为 [{}]，跳过物化", id, entity.getExecuteStatus());
            channel.basicNack(tag, false, false);
            return;
        }
        boolean cas = aiSkillPackageService.lambdaUpdate()
                .set(AiSkillPackageEntity::getExecuteStatus, ExecuteStatus.Processing.getValue())
                .eq(AiSkillPackageEntity::getId, id)
                .eq(AiSkillPackageEntity::getExecuteStatus, ExecuteStatus.Pending.getValue())
                .update();
        if (!cas) {
            log.info("ID 为 [{}] 的 Skill 目录未能 CAS 为执行中，跳过物化", id);
            channel.basicNack(tag, false, false);
            return;
        }
        try {
            AiSkillPackageEntity current = aiSkillPackageService.get(id);
            skillSourceResolvers.stream()
                    .filter(item -> item.isSupport(current.getSourceType()))
                    .findFirst()
                    .ifPresent(resolver -> resolver.ingest(current));
            aiSkillPackageService.lambdaUpdate()
                    .set(AiSkillPackageEntity::getExecuteStatus, ExecuteStatus.Success.getValue())
                    .eq(AiSkillPackageEntity::getId, id)
                    .update();
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("物化 ID 为 [{}] 的 Skill 目录失败", id, e);
            aiSkillPackageService.lambdaUpdate()
                    .set(AiSkillPackageEntity::getExecuteStatus, ExecuteStatus.Failure.getValue())
                    .eq(AiSkillPackageEntity::getId, id)
                    .update();
            channel.basicNack(tag, false, false);
        }
    }
}
