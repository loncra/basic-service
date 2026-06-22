package io.github.loncra.basic.service.message.server.resolver.support;

import com.rabbitmq.client.Channel;
import io.github.loncra.basic.service.auth.api.service.SystemUserServiceClient;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.AttachmentMessage;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.message.api.domian.metadata.MessageConstants;
import io.github.loncra.basic.service.message.api.enumerate.MessageTypeEnum;
import io.github.loncra.basic.service.message.server.config.MailConfig;
import io.github.loncra.basic.service.message.server.domain.body.email.EmailMessageBody;
import io.github.loncra.basic.service.message.server.domain.entity.BasicMessageEntity;
import io.github.loncra.basic.service.message.server.domain.entity.EmailMessageEntity;
import io.github.loncra.basic.service.message.server.resolver.AttachmentResolver;
import io.github.loncra.basic.service.message.server.resolver.MessageTypeResolver;
import io.github.loncra.basic.service.message.server.service.EmailMessageService;
import io.github.loncra.basic.service.resource.api.service.AttachmentServiceClient;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.id.metadata.IdNameMetadata;
import io.github.loncra.framework.commons.minio.FileObject;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.jndi.JndiLocatorDelegate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 邮件消息发送者实现
 *
 * @author maurice
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailMessageSenderResolver extends AbstractBatchMessageSenderResolver<EmailMessageBody, EmailMessageEntity> implements InitializingBean, AttachmentResolver, MessageTypeResolver {

    public static final String DEFAULT_QUEUE_NAME = "message.email.queue";

    private final Map<String, JavaMailSenderImpl> mailSenderMap = new LinkedHashMap<>();

    @Getter
    private final EmailMessageService emailMessageService;

    private final SystemUserServiceClient systemUserServiceClient;

    private final AmqpTemplate amqpTemplate;

    private final MailConfig config;

    private final AttachmentServiceClient attachmentServiceClient;

    @Override
    public RestResult<Object> send(List<EmailMessageEntity> entities) {
        entities.forEach(e -> amqpTemplate.convertAndSend(SystemConstants.SYS_MESSAGE_RABBITMQ_EXCHANGE, DEFAULT_QUEUE_NAME, e.getId()));
        return RestResult.ofSuccess(
                "发送 " + entities.size() + " 条邮件消息完成",
                entities.stream().map(BasicMessageEntity::getId).collect(Collectors.toList())
        );
    }

    @Override
    protected int getMaxRetryCount() {
        return config.getMaxRetryCount();
    }

    @Override
    protected List<EmailMessageEntity> getBatchMessageBodyContent(List<EmailMessageBody> result) {
        return result.stream().flatMap(this::createEmailMessageEntity).collect(Collectors.toList());
    }


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
     * 发送邮件
     *
     * @param id 邮件实体 id
     */
    @Override
    public EmailMessageEntity doSendMessage(Long id) {

        EmailMessageEntity entity = emailMessageService.get(id);

        if (Objects.isNull(entity)) {
            return null;
        }

        if (ExecuteStatus.Success.equals(entity.getExecuteStatus()) || ExecuteStatus.Failure.equals(entity.getExecuteStatus())) {
            return entity;
        }

        JavaMailSenderImpl mailSender = mailSenderMap.get(entity.getType().toString().toLowerCase());

        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(entity.getFromEmail(), config.getPersonal());
            helper.setTo(entity.getToEmail());
            helper.setSubject(entity.getTitle());
            helper.setText(entity.getContent(), true);

            if (CollectionUtils.isNotEmpty(entity.getAttachmentList())) {

                for (ObjectWriteResult fileObject : entity.getAttachmentList()) {
                    // FIXME 这里每次获取同名称的文件效率很低
                    byte[] file = attachmentServiceClient.getAttachmentFile(fileObject.getBucketName(), fileObject.getObjectName());
                    InputStreamSource iss = new ByteArrayResource(file);

                    helper.addAttachment(fileObject.getObjectName(), iss);
                }

            }

            mailSender.send(mimeMessage);

            ExecuteStatus.success(entity);
        } catch (Exception e) {
            if (!entity.isRetry()) {
                ExecuteStatus.failure(entity, e.getMessage());
            } else {
                ExecuteStatus.retry(entity, e.getMessage());
                throw new SystemException(e);
            }
        } finally {
            emailMessageService.save(entity);
        }

        if (Objects.nonNull(entity.getBatchId())) {
            syncBatchMessage(entity);
        }

        return entity;
    }

    @Override
    public List<EmailMessageEntity> preSend(List<EmailMessageEntity> content) {
        emailMessageService.save(content);
        return super.preSend(content);
    }

    /**
     * 通过邮件消息 body 构造邮件消息并保存信息
     *
     * @param body 邮件消息 body
     * @return 邮件消息流
     */
    private Stream<EmailMessageEntity> createEmailMessageEntity(EmailMessageBody body) {

        List<EmailMessageEntity> result = new LinkedList<>();

        for (String toEmail : body.getToEmails()) {

            EmailMessageEntity entity = ofEntity(body);
            entity.setToEmail(toEmail);

            if (Strings.CS.contains(toEmail, CacheProperties.DEFAULT_SEPARATOR) && ResourceSourceEnum.validate(toEmail)) {
                Map<String, Object> user = systemUserServiceClient.getSystemUser(toEmail);
                String phone = Objects.toString(user.get(PrincipalDetailsConstants.EMAIL_KEY), StringUtils.EMPTY);
                if(StringUtils.isEmpty(phone)) {
                    continue;
                }
                entity.setToEmail(phone);
                String systemName = Objects.toString(user.get(PrincipalDetailsConstants.SYSTEM_NAME_KEY));
                String name = PrincipalDetailsConstants.getPrincipalName(user);
                entity.getMetadata().put(BasicMessageEntity.TO_PRINCIPAL_METADATA_KEY, IdNameMetadata.of(systemName,name));
            } else if (Arrays.stream(ResourceSourceEnum.values()).anyMatch(r -> r.toString().equals(toEmail))) {
                Map<String, Object> filter = new LinkedHashMap<>();
                filter.put("filter_[email_nen]", true);
                filter.put("filter_[status_eq]", YesOrNo.Yes.getValue());
                List<Map<String, Object>> users = systemUserServiceClient.findSystemUser(toEmail, filter);
                for (Map<String, Object> user : users) {
                    String email =  Objects.toString(user.get(PrincipalDetailsConstants.EMAIL_KEY), StringUtils.EMPTY);
                    if (StringUtils.isEmpty(email)) {
                        continue;
                    }
                    entity = ofEntity(body);
                    entity.setToEmail(email);

                    String systemName = Objects.toString(user.get(PrincipalDetailsConstants.SYSTEM_NAME_KEY));
                    String name = PrincipalDetailsConstants.getPrincipalName(user);
                    entity.getMetadata().put(BasicMessageEntity.TO_PRINCIPAL_METADATA_KEY, IdNameMetadata.of(systemName,name));

                    result.add(entity);
                }
                continue;
            }

            result.add(entity);
        }

        return result.stream();
    }

    /**
     * 创建邮件消息实体
     *
     * @param body 邮件消息 body
     * @return 邮件消息实体
     */
    private EmailMessageEntity ofEntity(EmailMessageBody body) {
        EmailMessageEntity entity = CastUtils.of(body, EmailMessageEntity.class, AttachmentMessage.ATTACHMENT_LIST_FIELD);

        JavaMailSenderImpl mailSender = Objects.requireNonNull(
                mailSenderMap.get(entity.getType().toString().toLowerCase()),
                "找不到类型为 [" + entity.getType() + "] 的邮件发送者"
        );

        Assert.isTrue(StringUtils.isNotEmpty(mailSender.getUsername()), "类型为 [" + entity.getType().toString().toLowerCase() + "] 的邮件发送者 username 为空");

        entity.setFromEmail(mailSender.getUsername());

        if (CollectionUtils.isNotEmpty(body.getAttachmentList())) {
            entity.setAttachmentList(body.getAttachmentList());
        }

        return entity;
    }

    @Override
    public String getMessageType() {
        return MessageConstants.DEFAULT_EMAIL_TYPE_VALUE;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        config.getAccounts().entrySet().forEach(this::generateMailSender);
    }

    @Override
    public RestResult<Object> removeAttachment(FileObject fileObject) {
        MultiValueMap<String, Object> filter = new LinkedMultiValueMap<>();
        filter.add("filter_[attachment_list.bucketName_jeq]", fileObject.getBucketName());
        filter.add("filter_[attachment_list.objectName_jeq]", fileObject.getObjectName());
        List<EmailMessageEntity> list = emailMessageService.find(filter);
        for (EmailMessageEntity entity : list) {
            entity.getAttachmentList().removeIf(a -> a.getBucketName().equals(fileObject.getBucketName()) && a.getObjectName().equals(fileObject.getObjectName()));
            emailMessageService.save(entity);
        }
        return RestResult.ofSuccess("删除 [" + list.size() + "] 个邮箱消息附件成功", list.stream().map(IdEntity::getId).toList());
    }

    @Override
    public String getCategory() {
        return getMessageType();
    }

    @Override
    public List<MessageTypeEnum> getMessageTypeList(AuditAuthenticationToken token) {
        return Arrays.asList(MessageTypeEnum.CAPTCHA, MessageTypeEnum.SYSTEM, MessageTypeEnum.NOTICE, MessageTypeEnum.WARNING);
    }

    /**
     * 生成邮件发送者
     *
     * @param entry 账户配置信息
     */
    private void generateMailSender(Map.Entry<String, MailProperties> entry) {

        MailProperties mailProperties = entry.getValue();

        if (MapUtils.isEmpty(mailProperties.getProperties())) {
            mailProperties.getProperties().putAll(config.getProperties());
        }

        mailProperties.setHost(StringUtils.defaultIfEmpty(mailProperties.getHost(), config.getHost()));
        mailProperties.setPort(Objects.nonNull(mailProperties.getPort()) ? mailProperties.getPort() : config.getPort());
        mailProperties.setProtocol(StringUtils.defaultIfEmpty(mailProperties.getProtocol(), config.getProtocol()));
        mailProperties.setDefaultEncoding(Objects.nonNull(mailProperties.getDefaultEncoding()) ? mailProperties.getDefaultEncoding() : config.getDefaultEncoding());

        JavaMailSenderImpl mailSender = mailSenderMap.computeIfAbsent(
                entry.getKey(),
                k -> new JavaMailSenderImpl()
        );

        mailSender.setUsername(mailProperties.getUsername());
        mailSender.setPassword(mailProperties.getPassword());
        mailSender.getJavaMailProperties().putAll(mailProperties.getProperties());
        mailSender.setHost(mailProperties.getHost());
        mailSender.setPort(mailProperties.getPort());
        mailSender.setProtocol(mailProperties.getProtocol());
        mailSender.setDefaultEncoding(mailProperties.getDefaultEncoding().toString());

        String jndiName = StringUtils.defaultIfEmpty(mailProperties.getJndiName(), config.getJndiName());

        if (StringUtils.isNotBlank(jndiName)) {
            try {
                Session session = JndiLocatorDelegate.createDefaultResourceRefLocator().lookup(jndiName, Session.class);
                mailSender.setSession(session);
            } catch (Exception e) {
                throw new IllegalStateException(String.format("Unable to find Session in JNDI location %s", jndiName), e);
            }
        }

        if (log.isDebugEnabled()) {
            MessageTypeEnum messageTypeEnum = MessageTypeEnum.valueOf(entry.getKey().toUpperCase());
            log.debug("构造消息类型为:{}, 的邮箱完成，配置信息为:{}", messageTypeEnum.getName(), SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(mailProperties)));
        }

    }
}
