package io.github.loncra.basic.service.message.server.resolver.support.sms.alibaba;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.GetSmsTemplateResponseBody;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import io.github.loncra.basic.service.commons.enumerate.AuditStatusEnum;
import io.github.loncra.basic.service.commons.enumerate.CloudChannelEnum;
import io.github.loncra.basic.service.message.api.domian.metadata.MessageConstants;
import io.github.loncra.basic.service.message.api.domian.metadata.SmsBalanceMetadata;
import io.github.loncra.basic.service.message.api.enumerate.MessageTypeEnum;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsMessageBody;
import io.github.loncra.basic.service.message.server.domain.entity.SmsMessageEntity;
import io.github.loncra.basic.service.message.server.resolver.support.sms.SmsChannelSender;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.DateUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdNameValueMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 阿里云短信服务渠道发送者实现
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlibabaCloudSmsChannelSender implements SmsChannelSender {

    public static final int DEFAULT_PAGE_SIZE = 50;

    public static final List<String> AUDIT_STATE_INIT = List.of("AUDIT_STATE_INIT", "0");

    public static final List<String> AUDIT_STATE_PASS =  List.of("AUDIT_STATE_PASS", "1");

    public static final List<String> AUDIT_STATE_NOT_PASS = List.of("AUDIT_STATE_NOT_PASS", "2");

    public static final List<String> AUDIT_STATE_CANCEL = List.of("AUDIT_STATE_CANCEL","10");

    public static final Integer CAPTCHA_TYPE_VALUE = 0;

    public static final Integer NOTICE_TYPE_VALUE = 1;

    public static final Integer PROMOTION_TYPE_VALUE = 2;

    protected final PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper(
            PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_PREFIX,
            PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_SUFFIX
    );

    private final Client alibabaClient;

    private final AlibabaCloudSmsTemplateResolver alibabaCloudSmsTemplateResolver;

    @Override
    public CloudChannelEnum getType() {
        return CloudChannelEnum.ALIBABA_CLOUD;
    }

    @Override
    public RestResult<Map<String, Object>> sendSms(SmsMessageEntity entity) {
        Map<String, Object> metadata = CastUtils.convertValue(entity.getMetadata(), CastUtils.MAP_TYPE_REFERENCE);
        SendSmsRequest sendSmsRequest = new SendSmsRequest();

        String templateCode = Objects.toString(metadata.get(MessageConstants.Sms.TEMPLATE_CODE_FIELD), StringUtils.EMPTY);
        String signCode = Objects.toString(metadata.get(MessageConstants.Sms.SIGN_CODE_FIELD), StringUtils.EMPTY);

        sendSmsRequest.setOutId(entity.getId().toString());
        sendSmsRequest.setTemplateCode(templateCode);
        sendSmsRequest.setSignName(signCode);
        sendSmsRequest.setPhoneNumbers(entity.getPhoneNumber());

        String templateParam = SystemException.convertSupplier(
                () -> CastUtils.getObjectMapper().writeValueAsString(metadata.get(MessageConstants.VARIABLES_FIELD))
        );
        sendSmsRequest.setTemplateParam(templateParam);

        SendSmsResponse sendSmsResponse = SystemException.convertSupplier(
                () -> alibabaClient.sendSms(sendSmsRequest),
                "[" + getType().getName() + "] 发送验证码出现异常"
        );
        SystemException.isTrue(HttpStatus.OK.getReasonPhrase().equals(sendSmsResponse.getBody().getCode()), sendSmsResponse.getBody().getMessage());
        return RestResult.ofSuccess(CastUtils.convertValue(sendSmsResponse, CastUtils.MAP_TYPE_REFERENCE));
    }

    @Override
    public SmsBalanceMetadata getBalance() {
        throw new SystemException("[" + getType().getName() + "] 服务未提供查询余额接口对接");
    }

    public static Instant ofDateString(String dataString) {
        LocalDateTime localDateTime = LocalDateTime.parse(
                dataString,
                DateTimeFormatter.ofPattern(DateUtils.DEFAULT_DATE_TIME_FORMATTER_PATTERN)
        );

        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    public static AuditStatusEnum createAuditStatus(String auditStatus) {
        if (AUDIT_STATE_INIT.contains(auditStatus)) {
            return AuditStatusEnum.AUDITABLE;
        } else if (AUDIT_STATE_PASS.contains(auditStatus)) {
            return AuditStatusEnum.AGREED;
        } else if (AUDIT_STATE_NOT_PASS.contains(auditStatus)) {
            return AuditStatusEnum.DISAGREE;
        } else if (AUDIT_STATE_CANCEL.contains(auditStatus)) {
            return AuditStatusEnum.REJECTED;
        } else {
            return AuditStatusEnum.UNKNOWN;
        }
    }

    public static MessageTypeEnum createMessageType(Integer value) {
        if (CAPTCHA_TYPE_VALUE.equals(value)) {
            return MessageTypeEnum.CAPTCHA;
        } else if (NOTICE_TYPE_VALUE.equals(value)) {
            return MessageTypeEnum.NOTICE;
        } else if (PROMOTION_TYPE_VALUE.equals(value)) {
            return MessageTypeEnum.PROMOTION;
        } else {
            return MessageTypeEnum.UNKNOWN;
        }
    }

    @Override
    public SmsMessageBody createSmsMessageEntity(SmsMessageBody body) {

        if (StringUtils.isEmpty(body.getContent())) {
            Object code = body.getMetadata().get(MessageConstants.Sms.TEMPLATE_CODE_FIELD);
            String templateCode = Objects.toString(code, StringUtils.EMPTY);
            GetSmsTemplateResponseBody smsTemplate = alibabaCloudSmsTemplateResolver.get(templateCode);
            body.setContent(smsTemplate.getMessage());
            body.setType(createMessageType(NumberUtils.toInt(smsTemplate.getTemplateType())));
        }

        Properties templateVariables = new Properties();
        Object variable = body.getMetadata().get(MessageConstants.VARIABLES_FIELD);
        if (Objects.nonNull(variable) && variable instanceof List<?>) {
            List<IdNameValueMetadata<String, String>> variables = CastUtils.convertValue(variable, new TypeReference<>() {});
            variables.forEach(v -> templateVariables.put(v.getId(), v.getValue()));
        } else if (Objects.nonNull(variable) && variable instanceof Map<?,?>) {
            Map<String, Object> variables = CastUtils.convertValue(variable, CastUtils.MAP_TYPE_REFERENCE);
            templateVariables.putAll(variables);
        }
        body.getMetadata().put(MessageConstants.VARIABLES_FIELD, templateVariables);
        if (MapUtils.isNotEmpty(templateVariables)) {
            body.setContent(propertyPlaceholderHelper.replacePlaceholders(body.getContent(), templateVariables));
        }

        return body;
    }
}
