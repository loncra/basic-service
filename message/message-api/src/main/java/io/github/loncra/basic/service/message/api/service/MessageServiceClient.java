package io.github.loncra.basic.service.message.api.service;

import io.github.loncra.basic.service.message.api.domian.metadata.MessageConstants;
import io.github.loncra.basic.service.message.api.enumerate.MessageTypeEnum;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.security.audit.AuditPrincipal;
import io.micrometer.common.util.StringUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息发送服务的 Feign 到用接口
 *
 * @author maurice
 */
public interface MessageServiceClient {

    /**
     * 发送消息
     *
     * @param request 请求参数
     * @return rest 结果集
     */
    RestResult<Object> send(@RequestBody Map<String, Object> request);

    static Map<String, Object> createNoticeSiteMessage(List<AuditPrincipal> toUsers,
                                                       String title,
                                                       String content) {
        return createNoticeSiteMessage(toUsers, title, content, new LinkedHashMap<>());
    }

    static Map<String, Object> createNoticeSiteMessage(List<AuditPrincipal> toUsers,
                                                       String title,
                                                       String content,
                                                       Map<String, Object> metadata) {
        return createSiteMessage(toUsers, MessageTypeEnum.NOTICE, title, content, metadata);
    }

    static Map<String, Object> createSiteMessage(List<AuditPrincipal> toUsers,
                                                 MessageTypeEnum messageType,
                                                 String title,
                                                 String content) {
        return createSiteMessage(toUsers, messageType, title, content, new LinkedHashMap<>());
    }

    static Map<String, Object> createSiteMessage(List<AuditPrincipal> toUsers,
                                                 MessageTypeEnum messageType,
                                                 String title, String content,
                                                 Map<String, Object> metadata) {
        return createSiteMessage(toUsers, messageType, YesOrNo.No, title, content, metadata);
    }

    static Map<String, Object> createPushableNoticeSiteMessage(List<AuditPrincipal> toUsers,
                                                               String title,
                                                               String content) {
        return createPushableNoticeSiteMessage(toUsers, title, content, new LinkedHashMap<>());
    }

    static Map<String, Object> createPushableNoticeSiteMessage(List<AuditPrincipal> toUsers,
                                                               String title,
                                                               String content,
                                                               Map<String, Object> metadata) {
        return createPushableSiteMessage(toUsers, MessageTypeEnum.NOTICE, title, content, metadata);
    }

    static Map<String, Object> createPushableSiteMessage(List<AuditPrincipal> toUsers,
                                                         MessageTypeEnum messageType,
                                                         String title,
                                                         String content) {
        return createPushableSiteMessage(toUsers, messageType, title, content, new LinkedHashMap<>());
    }

    static Map<String, Object> createPushableSiteMessage(List<AuditPrincipal> toUsers,
                                                         MessageTypeEnum messageType,
                                                         String title,
                                                         String content,
                                                         Map<String, Object> metadata) {
        return createSiteMessage(toUsers, messageType, YesOrNo.Yes, title, content, metadata);
    }

    static Map<String, Object> createSiteMessage(List<AuditPrincipal> toUsers,
                                                 MessageTypeEnum messageType,
                                                 YesOrNo pushable,
                                                 String title,
                                                 String content,
                                                 Map<String, Object> metadata) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put(MessageConstants.Site.TO_USERS_FIELD, toUsers.stream().map(AuditPrincipal::getPrincipal).toList());
        map.put(TypeIdNameMetadata.TYPE_FIELD_NAME, messageType.getValue());
        map.put(MessageConstants.Site.IS_PUSHABLE_FIELD, pushable);

        map.put(MessageConstants.DEFAULT_MESSAGE_TYPE_KEY, MessageConstants.DEFAULT_SITE_TYPE_VALUE);

        map.put(MessageConstants.DEFAULT_TITLE_KEY, title);
        map.put(MessageConstants.DEFAULT_CONTENT_KEY, content);

        map.put(RestResult.DEFAULT_METADATA_NAME, metadata);

        return map;
    }

    static Map<String, Object> createSmsMessage(List<String> phoneNumbers, String channel, Map<String, Object> metadata) {
        SystemException.isTrue(MapUtils.isNotEmpty(metadata), "metadata cannot be empty");
        SystemException.isTrue(StringUtils.isNotEmpty(channel), "channel cannot be empty");
        Map<String, Object> param = new LinkedHashMap<>();

        param.put(RestResult.DEFAULT_METADATA_NAME, metadata);
        param.put(MessageConstants.Sms.CHANNEL_FIELD, channel);
        param.put(MessageConstants.Sms.PHONE_NUMBERS_FIELD, phoneNumbers);
        param.put(MessageConstants.DEFAULT_MESSAGE_TYPE_KEY, MessageConstants.DEFAULT_SMS_TYPE_VALUE);

        return param;
    }

    static Map<String, Object> createEmailMessage(List<String> email, String title, String content, MessageTypeEnum messageType) {
        Map<String, Object> param = new LinkedHashMap<>();
        param.put(MessageConstants.DEFAULT_TITLE_KEY, title);
        param.put(MessageConstants.DEFAULT_CONTENT_KEY, content);
        param.put(MessageConstants.Email.TO_EMAILS_FIELD, email);
        param.put(TypeIdNameMetadata.TYPE_FIELD_NAME, messageType);

        param.put(MessageConstants.DEFAULT_MESSAGE_TYPE_KEY, MessageConstants.DEFAULT_EMAIL_TYPE_VALUE);

        return param;
    }

}
