package io.github.loncra.basic.service.message.server.resolver.support.sms.support;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.*;
import com.fasterxml.jackson.core.type.TypeReference;
import io.github.loncra.basic.service.commons.enumerate.AuditStatusEnum;
import io.github.loncra.basic.service.commons.enumerate.CloudChannelEnum;
import io.github.loncra.basic.service.message.api.domian.metadata.*;
import io.github.loncra.basic.service.message.api.enumerate.MessageTypeEnum;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsMessageBody;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsSignResponseBody;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsTemplateResponseBody;
import io.github.loncra.basic.service.message.server.domain.entity.SmsMessageEntity;
import io.github.loncra.basic.service.message.server.enumerate.AlibabaCloudSmsSignTagEnum;
import io.github.loncra.basic.service.message.server.resolver.support.sms.SmsChannelSender;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.DateUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdNameValueMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
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

    public static final String QUALIFICATION_ID_KEY = "qualificationId";

    public static final String FILE_URL_LIST_KEY = "fileUrlList";

    public static final String MORE_DATA_FILE_URL_LIST_KEY = "moreDataFileUrlList";

    public static final String SIGN_CODE_KEY = "signCode";

    public static final String SIGN_TAG_KEY = "signTag";

    public static final String APPLY_SCENE_KEY = "applyScene";

    public static final String THIRD_PARTY_KEY = "thirdParty";

    public static final String SIGN_USAGE_KEY = "signUsage";

    public static final String REGISTER_RESULT_KEY = "registerResult";

    public static final String VARIABLE_ATTRIBUTE_KEY = "variableAttribute";

    public static final String RELATED_SIGN_NAME_KEY = "relatedSignName";

    public static final String TEMPLATE_TAG_KEY = "templateTag";

    protected final PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper(
            PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_PREFIX,
            PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_SUFFIX
    );

    private final Client alibabaClient;

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
                () -> CastUtils.getObjectMapper().writeValueAsString(metadata.get(MessageConstants.VARIABLE_FIELD))
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

    @Override
    public List<SmsSignResponseBody> signList() {
        QuerySmsSignListRequest querySmsSignListRequest = new QuerySmsSignListRequest();

        querySmsSignListRequest.setPageIndex(1);
        querySmsSignListRequest.setPageSize(DEFAULT_PAGE_SIZE);

        QuerySmsSignListResponse response = SystemException.convertSupplier(
                () -> alibabaClient.querySmsSignList(querySmsSignListRequest),
                "[" + getType().getName() + "] 获取短信签名列表出现错误"
        );
        SystemException.isTrue(
                HttpStatus.OK.getReasonPhrase().equals(response.getBody().getCode()),
                response.getBody().getMessage()
        );

        List<QuerySmsSignListResponseBody.QuerySmsSignListResponseBodySmsSignList> data;
        data = response.getBody().getSmsSignList();
        if(CollectionUtils.isNotEmpty(data)) {
            return data.stream().map(this::createSmsSignResponseBody).toList();
        }

        return Collections.emptyList();
    }

    private SmsSignResponseBody createSmsSignResponseBody(QuerySmsSignListResponseBody.QuerySmsSignListResponseBodySmsSignList smsSign) {
        SmsSignResponseBody body = new SmsSignResponseBody();
        body.setName(smsSign.getSignName());
        body.setChannel(getType());
        body.setStatus(createAuditStatus(smsSign.getAuditStatus()));
        body.setCreationTime(ofDateString(smsSign.getCreateDate()));
        body.setId(smsSign.getSignName());

        body.getMetadata().put(APPLY_SCENE_KEY, smsSign.getBusinessType());

        setAuditData(smsSign.getReason(),body);

        return body;
    }

    private SmsSignResponseBody createSmsSignResponseBody(GetSmsSignResponseBody responseBody) {
        SmsSignResponseBody body = new SmsSignResponseBody();

        body.setName(responseBody.getSignName());
        body.setChannel(getType());
        body.setStatus(createAuditStatus(responseBody.getSignStatus().toString()));
        body.setCreationTime(ofDateString(responseBody.getCreateDate()));
        body.setId(responseBody.getSignCode());
        body.setRemark(responseBody.getRemark());

        body.getMetadata().put(QUALIFICATION_ID_KEY, responseBody.getQualificationId());
        body.getMetadata().put(FILE_URL_LIST_KEY, responseBody.getFileUrlList());
        body.getMetadata().put(SIGN_CODE_KEY, responseBody.getSignCode());
        body.getMetadata().put(APPLY_SCENE_KEY, responseBody.getApplyScene());
        body.getMetadata().put(THIRD_PARTY_KEY, YesOrNo.ofBoolean(!responseBody.getThirdParty()));
        body.getMetadata().put(SIGN_USAGE_KEY, responseBody.getSignUsage());
        body.getMetadata().put(REGISTER_RESULT_KEY, responseBody.getRegisterResult());

        AlibabaCloudSmsSignTagEnum tag = ValueEnum.ofEnum(
                AlibabaCloudSmsSignTagEnum.class,
                responseBody.getSignTag(),
                true
        );
        body.getMetadata().put(SIGN_TAG_KEY, tag);

        setAuditData(responseBody.getAuditInfo(), body);
        return body;
    }

    private Instant ofDateString(String dataString) {
        LocalDateTime localDateTime = LocalDateTime.parse(
                dataString,
                DateTimeFormatter.ofPattern(DateUtils.DEFAULT_DATE_TIME_FORMATTER_PATTERN)
        );

        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    private AuditStatusEnum createAuditStatus(String auditStatus) {
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

    @Override
    public SmsSignResponseBody saveSign(SmsSignMetadata metadata) {
        throw new SystemException("不支持创建签名操作");
    }

    @Override
    public void deleteSign(List<String> ids) {
        List<String> errorMessage = new LinkedList<>();
        for (String id : ids) {
            DeleteSmsSignRequest request = new DeleteSmsSignRequest();
            request.setSignName(id);
            try {
                DeleteSmsSignResponse response = alibabaClient.deleteSmsSign(request);
                SystemException.isTrue(
                        HttpStatus.OK.getReasonPhrase().equals(response.getBody().getCode()),
                        response.getBody().getMessage()
                );
            } catch (Exception e) {
                log.warn("[" + getType().getName() + "] 删除签名错误", e);
                errorMessage.add(e.getMessage());
            }

        }
        SystemException.isTrue(CollectionUtils.isEmpty(errorMessage), StringUtils.join(errorMessage, CastUtils.COMMA));
    }

    @Override
    public SmsSignResponseBody getSign(String id) {
        GetSmsSignRequest getSmsSignRequest = new GetSmsSignRequest();
        getSmsSignRequest.setSignName(id);
        GetSmsSignResponse response = SystemException.convertSupplier(
                () -> alibabaClient.getSmsSign(getSmsSignRequest),
                "[" + getType().getName() + "] 获取短信签名错误"
        );
        SystemException.isTrue(
                HttpStatus.OK.getReasonPhrase().equals(response.getBody().getCode()),
                response.getBody().getMessage()
        );
        return createSmsSignResponseBody(response.getBody());

    }

    @Override
    public List<SmsTemplateResponseBody> templateList() {
        QuerySmsTemplateListRequest request = new QuerySmsTemplateListRequest();

        request.setPageIndex(1);
        request.setPageSize(DEFAULT_PAGE_SIZE);

        QuerySmsTemplateListResponse response = SystemException.convertSupplier(
                () -> alibabaClient.querySmsTemplateList(request),
                "[" + getType().getName() + "] 获取短信模版集合错误"
        );
        SystemException.isTrue(
                HttpStatus.OK.getReasonPhrase().equals(response.getBody().getCode()),
                response.getBody().getMessage()
        );
        List<QuerySmsTemplateListResponseBody.QuerySmsTemplateListResponseBodySmsTemplateList> data;
        data = response.getBody().getSmsTemplateList();
        if (CollectionUtils.isNotEmpty(data)) {
            return data.stream().map(this::createSmsTemplateResponseBody).toList();
        }
        return Collections.emptyList();
    }

    @Override
    public SmsTemplateResponseBody getTemplate(String id) {
        GetSmsTemplateRequest request = new GetSmsTemplateRequest();
        request.setTemplateCode(id);
        GetSmsTemplateResponse response = SystemException.convertSupplier(
                () -> alibabaClient.getSmsTemplate(request),
                "[" + getType().getName() + "] 获取短信模版错误"
        );
        SystemException.isTrue(
                HttpStatus.OK.getReasonPhrase().equals(response.getBody().getCode()),
                response.getBody().getMessage()
        );

        return createSmsTemplateResponseBody(response.getBody());
    }

    @Override
    public void deleteTemplate(List<String> ids) {
        DeleteSmsTemplateRequest request = new DeleteSmsTemplateRequest();
        List<String> errorMessage = new LinkedList<>();
        for (String id : ids) {
            request.setTemplateCode(id);

            try {
                DeleteSmsTemplateResponse response = alibabaClient.deleteSmsTemplate(request);
                SystemException.isTrue(
                        HttpStatus.OK.getReasonPhrase().equals(response.getBody().getCode()),
                        response.getBody().getMessage()
                );
            } catch (Exception e) {
                log.warn("[{}] 删除短信模版错误", getType().getName(), e);
                errorMessage.add(e.getMessage());
            }
        }
        SystemException.isTrue(CollectionUtils.isEmpty(errorMessage), StringUtils.join(errorMessage, CastUtils.COMMA));
    }

    @Override
    public SmsTemplateResponseBody saveTemplate(SmsTemplateMetadata metadata) {
        throw new SystemException("[" + getType().getName() + "] 不支持创建模版操作");
    }

    private SmsTemplateResponseBody createSmsTemplateResponseBody(GetSmsTemplateResponseBody item) {
        SmsTemplateResponseBody body = new SmsTemplateResponseBody();

        body.setName(item.getTemplateName());
        body.setChannel(getType());
        body.setContent(item.getTemplateContent());
        body.setType(createMessageType(NumberUtils.toInt(item.getTemplateType())));
        body.setCreationTime(ofDateString(item.getCreateDate()));
        body.setStatus(createAuditStatus(item.getTemplateStatus()));
        body.setId(item.getTemplateCode());
        body.setRemark(item.getRemark());

        setAuditData(item.getAuditInfo(), body);

        if (Objects.nonNull(item.getFileUrlList())) {
            body.getMetadata().put(FILE_URL_LIST_KEY, item.getFileUrlList().getFileUrl());
        }

        if (Objects.nonNull(item.getMoreDataFileUrlList())) {
            body.getMetadata().put(MORE_DATA_FILE_URL_LIST_KEY, item.getMoreDataFileUrlList().getMoreDataFileUrl());
        }

        body.getMetadata().put(APPLY_SCENE_KEY, item.getApplyScene());
        body.getMetadata().put(VARIABLE_ATTRIBUTE_KEY, item.getVariableAttribute());
        body.getMetadata().put(RELATED_SIGN_NAME_KEY, item.getRelatedSignName());
        body.getMetadata().put(TEMPLATE_TAG_KEY, ValueEnum.ofEnum(AlibabaCloudSmsSignTagEnum.class, item.getTemplateTag().toString()));

        return body;
    }

    private SmsTemplateResponseBody createSmsTemplateResponseBody(QuerySmsTemplateListResponseBody.QuerySmsTemplateListResponseBodySmsTemplateList item) {
        SmsTemplateResponseBody body = new SmsTemplateResponseBody();

        body.setName(item.getTemplateName());
        body.setChannel(getType());
        body.setContent(item.getTemplateContent());
        body.setType(createMessageType(item.getOuterTemplateType()));
        body.setCreationTime(ofDateString(item.getCreateDate()));
        body.setStatus(createAuditStatus(item.getAuditStatus()));
        body.setId(item.getTemplateCode());
        setAuditData(item.getReason(), body);

        return body;
    }

    private void setAuditData(Object reason, AlibabaCloudAuditMetadata metadata) {
        if (Objects.isNull(reason)) {
            return;
        }
        Map<String, String> rejectInfo = new LinkedHashMap<>();
        Field field = ReflectionUtils.findField(reason.getClass(), AlibabaCloudAuditMetadata.REJECT_INFO_KEY);
        if (Objects.isNull(field)) {
            return ;
        }
        ReflectionUtils.makeAccessible(field);
        String info = Objects.toString(ReflectionUtils.getField(field, reason), StringUtils.EMPTY);
        rejectInfo.put(AlibabaCloudAuditMetadata.REJECT_INFO_KEY, info);

        /*Object subjInfoObject = SystemException.convertSupplier(() -> ReflectionUtils.getFieldValue(reason, AlibabaCloudAuditMetadata.REJECT_SUB_INFO_KEY));
        String subInfo = Objects.toString(subjInfoObject, StringUtils.EMPTY);
        rejectInfo.put(AlibabaCloudAuditMetadata.REJECT_SUB_INFO_KEY, subInfo);*/

        metadata.getMetadata().put(AlibabaCloudAuditMetadata.INFO_FIELD_KEY, rejectInfo);

        /*Object rejectDate = SystemException.convertSupplier(() ->ReflectionUtils.getFieldValue(reason, AlibabaCloudAuditMetadata.REJECT_DATE_KEY));
        String auditionTime = Objects.toString(rejectDate, StringUtils.EMPTY);
        if (StringUtils.isNotEmpty(auditionTime)) {
            metadata.setAuditionTime(ofDateString(auditionTime));
        }*/
    }

    private MessageTypeEnum createMessageType(Integer value) {
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
        if (StringUtils.isNotEmpty(body.getContent()) && Objects.nonNull(body.getType())) {
            return body;
        }

        Object code = body.getMetadata().get(MessageConstants.Sms.TEMPLATE_CODE_FIELD);
        String templateCode = Objects.toString(code, StringUtils.EMPTY);
        SmsTemplateResponseBody smsTemplate = getTemplate(templateCode);
        if (Objects.isNull(smsTemplate)) {
            return body;
        }

        if (StringUtils.isEmpty(body.getContent())) {
            body.setContent(smsTemplate.getContent());
        }

        Properties properties = new Properties();
        Object variable = body.getMetadata().get(MessageConstants.VARIABLE_FIELD);
        if (Objects.nonNull(variable) && variable instanceof List<?>) {
            List<IdNameValueMetadata<String, String>> variables = CastUtils.convertValue(variable, new TypeReference<>() {});
            variables.forEach(v -> properties.put(v.getId(), v.getValue()));
        } else if (Objects.nonNull(variable) && variable instanceof Map<?,?>) {
            Map<String, Object> variables = CastUtils.convertValue(variable, CastUtils.MAP_TYPE_REFERENCE);
            properties.putAll(variables);
        }

        if (MapUtils.isNotEmpty(properties)) {
            body.setContent(propertyPlaceholderHelper.replacePlaceholders(body.getContent(), properties));
        }

        if (Objects.isNull(body.getType())) {
            body.setType(smsTemplate.getType());
        }

        return body;
    }
}
