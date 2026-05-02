package io.github.loncra.basic.service.message.server.resolver.support;

import com.rabbitmq.client.Channel;
import io.github.loncra.basic.service.auth.api.service.SystemUserServiceClient;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.message.api.domian.metadata.MessageConstants;
import io.github.loncra.basic.service.message.api.enumerate.MessageTypeEnum;
import io.github.loncra.basic.service.message.server.config.SmsConfig;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsMessageBody;
import io.github.loncra.basic.service.message.server.domain.entity.BasicMessageEntity;
import io.github.loncra.basic.service.message.server.domain.entity.SmsMessageEntity;
import io.github.loncra.basic.service.message.server.resolver.MessageTypeResolver;
import io.github.loncra.basic.service.message.server.resolver.support.sms.SmsChannelSender;
import io.github.loncra.basic.service.message.server.service.SmsMessageService;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.ServiceException;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.id.metadata.IdNameValueMetadata;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 短信消息发送者实现
 *
 * @author maurice
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsMessageSenderResolver extends AbstractBatchMessageSenderResolver<SmsMessageBody, SmsMessageEntity> implements MessageTypeResolver {

    public static final String BATCH_UPDATE_CONCURRENT_KEY = "loncra:basic-service:message:sms:batch-update:";

    public static final String DEFAULT_QUEUE_NAME = "message.sms.queue";

    @Getter
    private final SmsMessageService smsMessageService;

    @Getter
    private final List<SmsChannelSender> smsChannelSenderList;

    private final SystemUserServiceClient systemUserServiceClient;

    private final SmsConfig config;

    private final ConcurrentInterceptor concurrentInterceptor;

    private final AmqpTemplate amqpTemplate;

    @Override
    protected int getMaxRetryCount() {
        return config.getMaxRetryCount();
    }

    /**
     * 发送短信
     *
     * @param id 短信实体 id
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_MESSAGE_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void onMessage(Long id,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        super.sendMessage(id);
        channel.basicAck(tag, false);
    }

    /**
     * 发送短信
     *
     * @param id 短信实体 id
     * @return 短信消息实体
     */
    @Override
    public SmsMessageEntity doSendMessage(Long id) {

        SmsMessageEntity entity = smsMessageService.get(id);

        if (Objects.isNull(entity)) {
            return null;
        }

        if (ExecuteStatus.Success.equals(entity.getExecuteStatus())) {
            return entity;
        }

        SmsChannelSender smsChannelSender = getSmsChannelSender(config.getChannel());

        entity.setChannel(smsChannelSender.getType().getValue());

        try {

            RestResult<Map<String, Object>> restResult = smsChannelSender.sendSms(entity);

            if (restResult.getStatus() == HttpStatus.OK.value()) {
                ExecuteStatus.success(entity);
            } else if (restResult.getStatus() == HttpStatus.PROCESSING.value() && log.isDebugEnabled()) {
                log.debug("ID 为 [{}] ({}:{}) 的短信数据正在处理中.", entity.getId(), entity.getPhoneNumber(), entity.getContent());
            } else if (!entity.isRetry()) {
                ExecuteStatus.failure(entity, restResult.getMessage());
            } else {
                entity.setExecuteStatus(ExecuteStatus.Retrying);
            }
        } catch (Exception e) {
            if (!entity.isRetry()) {
                ExecuteStatus.failure(entity, e.getMessage());
            } else {
                ExecuteStatus.retry(entity, e.getMessage());
                throw new SystemException(e);
            }
        } finally {
            smsMessageService.save(entity);
        }


        if (Objects.nonNull(entity.getBatchId())) {
            concurrentInterceptor.invoke(BATCH_UPDATE_CONCURRENT_KEY + entity.getId(), () -> updateBatchMessage(entity));
        }

        return entity;
    }

    /**
     * 获取发送短信的渠道发送者
     *
     * @param channel 渠道类型
     * @return 短信渠道发送者
     */
    public SmsChannelSender getSmsChannelSender(String channel) {
        return smsChannelSenderList
                .stream()
                .filter(s -> channel.equals(s.getType().getValue()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到渠道为[" + channel + "]的短信渠道支持"));
    }

    @Override
    public List<SmsMessageEntity> preSend(List<SmsMessageEntity> content) {
        smsMessageService.save(content);
        return super.preSend(content);
    }

    @Override
    public RestResult<Object> send(List<SmsMessageEntity> entities) {

       entities.forEach(e -> amqpTemplate.convertAndSend(SystemConstants.SYS_MESSAGE_RABBITMQ_EXCHANGE, DEFAULT_QUEUE_NAME, e.getId()));

        return RestResult.ofSuccess(
                "发送 " + entities.size() + " 条短信消息完成",
                entities.stream().map(BasicMessageEntity::getId).collect(Collectors.toList())
        );
    }

    @Override
    protected List<SmsMessageEntity> getBatchMessageBodyContent(List<SmsMessageBody> result) {
        return result.stream().flatMap(this::createSmsMessageEntity).collect(Collectors.toList());
    }

    /**
     * 通过短信消息 body 构造短信消息并保存信息
     *
     * @param body 短信消息 body
     * @return 短信消息流
     */
    private Stream<SmsMessageEntity> createSmsMessageEntity(SmsMessageBody body) {
        SmsChannelSender sender = getSmsChannelSender(body.getChannel());
        body = sender.createSmsMessageEntity(body);

        List<SmsMessageEntity> result = new LinkedList<>();

        for (String phoneNumber : body.getPhoneNumbers()) {

            SmsMessageEntity entity = CastUtils.of(body, SmsMessageEntity.class);
            entity.setPhoneNumber(phoneNumber);
            if (Strings.CS.contains(phoneNumber, CacheProperties.DEFAULT_SEPARATOR) && ResourceSourceEnum.validate(phoneNumber)) {
                TypeIdNameMetadata principal = TypeIdNameMetadata.ofPrincipalString(phoneNumber);
                Map<String, Object> user = systemUserServiceClient.getSystemUser(principal);
                Long userId = CastUtils.cast(user.get(IdEntity.ID_FIELD_NAME), Long.class);
                String phone = Objects.toString(user.get(PrincipalDetailsConstants.PHONE_NUMBER_KEY), StringUtils.EMPTY);
                if(StringUtils.isEmpty(phone)) {
                    continue;
                }
                entity.setPhoneNumber(phone);
                IdNameValueMetadata<Long, String> metadata = new IdNameValueMetadata<>(
                        IdValueMetadata.of(userId, PrincipalDetailsConstants.getPrincipalName(user)),
                        principal.getType()
                );
                entity.getMetadata().put(BasicMessageEntity.TO_PRINCIPAL_METADATA_KEY, metadata);
            } else if (Arrays.stream(ResourceSourceEnum.values()).anyMatch(r -> r.toString().equals(phoneNumber))) {
                Map<String, Object> filter = new LinkedHashMap<>();
                filter.put("filter_[phone_number_nen]", true);
                filter.put("filter_[status_eq]", YesOrNo.Yes.getValue());
                List<String> phoneNumbers = systemUserServiceClient
                        .findSystemUser(phoneNumber, filter)
                        .stream()
                        .map(m -> Objects.toString(m.get(PrincipalDetailsConstants.PHONE_NUMBER_KEY), StringUtils.EMPTY))
                        .toList();
                for (String phone : phoneNumbers) {
                    entity = CastUtils.of(body, SmsMessageEntity.class);
                    entity.setPhoneNumber(phone);
                    result.add(entity);
                }
            }

            result.add(entity);
        }

        return result.stream();
    }

    @Override
    public String getMessageType() {
        return MessageConstants.DEFAULT_SMS_TYPE_VALUE;
    }


    @Override
    public String getCategory() {
        return MessageConstants.DEFAULT_SMS_TYPE_VALUE;
    }

    @Override
    public List<MessageTypeEnum> getMessageTypeList(AuditAuthenticationToken token) {
        return Arrays.asList(MessageTypeEnum.CAPTCHA, MessageTypeEnum.NOTICE, MessageTypeEnum.PROMOTION);
    }
}
