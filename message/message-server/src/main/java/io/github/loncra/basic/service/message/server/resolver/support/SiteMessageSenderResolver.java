package io.github.loncra.basic.service.message.server.resolver.support;

import io.github.loncra.basic.service.auth.api.service.SystemUserServiceClient;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.message.api.domian.metadata.MessageConstants;
import io.github.loncra.basic.service.message.api.enumerate.MessageTypeEnum;
import io.github.loncra.basic.service.message.server.config.SiteConfig;
import io.github.loncra.basic.service.message.server.domain.body.site.SiteMessageBody;
import io.github.loncra.basic.service.message.server.domain.entity.BasicMessageEntity;
import io.github.loncra.basic.service.message.server.domain.entity.SiteMessageEntity;
import io.github.loncra.basic.service.message.server.enumerate.UnreadQuantityGroupEnum;
import io.github.loncra.basic.service.message.server.resolver.AttachmentResolver;
import io.github.loncra.basic.service.message.server.resolver.MessageTypeResolver;
import io.github.loncra.basic.service.message.server.resolver.UnreadQuantityMessageResolver;
import io.github.loncra.basic.service.message.server.resolver.support.site.SiteMessageChannelSender;
import io.github.loncra.basic.service.message.server.service.SiteMessageService;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.id.metadata.IdNameMetadata;
import io.github.loncra.framework.commons.minio.FileObject;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.Validator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 站内信消息发送者
 *
 * @author maurice
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SiteMessageSenderResolver extends AbstractBatchMessageSenderResolver<SiteMessageBody, SiteMessageEntity> implements AttachmentResolver, MessageTypeResolver, UnreadQuantityMessageResolver {

    public static final String DEFAULT_QUEUE_NAME = "message.site.queue";

    private final SystemUserServiceClient systemUserServiceClient;

    @Getter
    private final SiteMessageService siteMessageService;

    private final List<SiteMessageChannelSender> siteMessageChannelSenderList;

    private final AmqpTemplate amqpTemplate;

    private final SiteConfig config;

    @Override
    @Qualifier("mvcValidator")
    @Autowired(required = false)
    public void setValidator(Validator validator) {
        super.setValidator(validator);
    }


    @Override
    @Autowired
    public void setThreadPoolTaskExecutor(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        super.setThreadPoolTaskExecutor(threadPoolTaskExecutor);
    }

    @Override
    protected int getMaxRetryCount() {
        return config.getMaxRetryCount();
    }

    /**
     * 发送站内信
     *
     * @param id 站内信实体 id
     */
    public void onMessage(Long id) {
        super.sendMessage(id);
    }

    /**
     * 发送站内信
     *
     * @param id 站内信实体 id
     */
    @Override
    public SiteMessageEntity doSendMessage(Long id) {

        SiteMessageEntity entity = siteMessageService.get(id);

        if (Objects.isNull(entity)) {
            return null;
        }

        if (ExecuteStatus.Success.equals(entity.getExecuteStatus())) {
            return entity;
        }

        List<SiteMessageChannelSender> siteMessageChannelSenders = getSiteMessageChannelSender(config.getChannel());

        //entity.setChannel(config.getChannel());

        try {
            Map<String, RestResult<Map<String, Object>>> restResults = new LinkedHashMap<>();
            for (SiteMessageChannelSender sender : siteMessageChannelSenders) {
                RestResult<Map<String, Object>> result = sender.sendSiteMessage(entity);
                restResults.put(sender.getType(), result);
            }

            if (restResults.values().stream().allMatch(r -> config.getSuccessStatus().contains(r.getStatus()))) {
                ExecuteStatus.success(entity);
            } else {
                List<String> messages = restResults
                        .entrySet()
                        .stream()
                        .filter(e -> e.getValue().getStatus() != HttpStatus.OK.value())
                        .map(e -> e.getKey() + CacheProperties.DEFAULT_SEPARATOR + e.getValue().getMessage())
                        .collect(Collectors.toList());
                ExecuteStatus.failure(entity, StringUtils.join(messages, CastUtils.COMMA));
            }

        } catch (Exception e) {
            if (!entity.isRetry()) {
                ExecuteStatus.failure(entity, e.getMessage());
            } else {
                ExecuteStatus.retry(entity, e.getMessage());
                throw new SystemException(e);
            }
        } finally {
            siteMessageService.save(entity);
        }

        if (Objects.nonNull(entity.getBatchId())) {
            syncBatchMessage(entity);
        }

        return entity;
    }

    /**
     * 获取站内信消息渠道发送者
     *
     * @param channel 渠道类型
     * @return 站内信消息渠道发送者
     */
    private List<SiteMessageChannelSender> getSiteMessageChannelSender(List<String> channel) {
        return siteMessageChannelSenderList
                .stream()
                .filter(s -> channel.contains(s.getType()))
                .collect(Collectors.toList());
    }

    /**
     * 通过站内信消息 body 构造站内信消息并保存信息
     *
     * @param body 站内信消息 body
     * @return 邮件消息流
     */
    private Stream<SiteMessageEntity> createSiteMessage(SiteMessageBody body) {

        List<SiteMessageEntity> result = new LinkedList<>();

        for (String user : body.getToUsers()) {
            if (Arrays.stream(ResourceSourceEnum.values()).anyMatch(r -> r.toString().equals(user))) {
                Map<String, Object> filter = new LinkedHashMap<>();
                filter.put("filter_[status_eq]", YesOrNo.Yes.getValue());
                List<Map<String, Object>> users = systemUserServiceClient.findSystemUser(user, filter);
                for (Map<String, Object> u : users) {
                    String toUsers = Objects.toString(u.get(PrincipalDetailsConstants.SYSTEM_NAME_KEY), StringUtils.EMPTY);
                    if (StringUtils.isEmpty(toUsers)) {
                        continue;
                    }
                    SiteMessageEntity entity = ofEntity(body);
                    entity.setToUser(toUsers);

                    String systemName = Objects.toString(u.get(PrincipalDetailsConstants.SYSTEM_NAME_KEY));
                    String name = PrincipalDetailsConstants.getPrincipalName(u);
                    entity.getMetadata().put(BasicMessageEntity.TO_PRINCIPAL_METADATA_KEY, IdNameMetadata.of(systemName,name));

                    result.add(entity);
                }
            } else {

                SiteMessageEntity entity = ofEntity(body);
                entity.setToUser(user);
                result.add(entity);
            }
        }

        return result.stream();
    }

    /**
     * 创建站内信消息实体
     *
     * @param body 站内信消息 body
     * @return 站内信消息实体
     */
    private SiteMessageEntity ofEntity(SiteMessageBody body) {
        SiteMessageEntity entity = CastUtils.of(body, SiteMessageEntity.class, SystemConstants.ATTACHMENT_FIELD_NAME);

        if (CollectionUtils.isNotEmpty(body.getAttachmentList())) {
            entity.setAttachmentList(body.getAttachmentList());
        }

        return entity;
    }

    @Override
    public String getMessageType() {
        return MessageConstants.DEFAULT_SITE_TYPE_VALUE;
    }

    @Override
    public List<SiteMessageEntity> preSend(List<SiteMessageEntity> content) {
        siteMessageService.save(content);
        return super.preSend(content);
    }

    @Override
    public RestResult<Object> send(List<SiteMessageEntity> entities) {
        entities.forEach(e -> amqpTemplate.convertAndSend(SystemConstants.SYS_MESSAGE_RABBITMQ_EXCHANGE, DEFAULT_QUEUE_NAME, e.getId()));

        return RestResult.ofSuccess(
                "发送 " + entities.size() + " 条站内信消息完成",
                entities.stream().map(BasicMessageEntity::getId).collect(Collectors.toList())
        );
    }

    @Override
    protected List<SiteMessageEntity> getBatchMessageBodyContent(List<SiteMessageBody> result) {
        return result.stream().flatMap(this::createSiteMessage).collect(Collectors.toList());
    }

    @Override
    public String getCategory() {
        return getMessageType();
    }

    @Override
    public List<MessageTypeEnum> getMessageTypeList(AuditAuthenticationToken token) {
        return Arrays.asList(MessageTypeEnum.SYSTEM, MessageTypeEnum.NOTICE, MessageTypeEnum.WARNING);
    }

    @Override
    public RestResult<Object> removeAttachment(FileObject fileObject) {
        MultiValueMap<String, Object> filter = new LinkedMultiValueMap<>();
        filter.add("filter_[attachment_list.bucketName_jeq]", fileObject.getBucketName());
        filter.add("filter_[attachment_list.objectName_jeq]", fileObject.getObjectName());
        List<SiteMessageEntity> list = siteMessageService.find(filter);
        for (SiteMessageEntity entity : list) {
            entity.getAttachmentList().removeIf(a -> a.getBucketName().equals(fileObject.getBucketName()) && a.getObjectName().equals(fileObject.getObjectName()));
            siteMessageService.save(entity);
        }
        return RestResult.ofSuccess("删除 [" + list.size() + "] 个站内信附件成功", list.stream().map(IdEntity::getId).toList());
    }

    @Override
    public Map<Long, Object> countUnreadQuantity(AuditAuthenticationToken token) {
        return new LinkedHashMap<>(siteMessageService.countUnreadQuantity(token));
    }

    @Override
    public UnreadQuantityGroupEnum getGroup() {
        return UnreadQuantityGroupEnum.SITE;
    }
}
