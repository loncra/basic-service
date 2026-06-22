package io.github.loncra.basic.service.resource.server.service.captcha;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.github.loncra.basic.service.commons.enumerate.TimeUnitEnum;
import io.github.loncra.basic.service.message.api.domian.metadata.MessageConstants;
import io.github.loncra.basic.service.message.api.enumerate.MessageTypeEnum;
import io.github.loncra.basic.service.message.api.service.MessageServiceClient;
import io.github.loncra.basic.service.resource.api.domain.metadata.DataDictionaryMetadata;
import io.github.loncra.basic.service.resource.server.config.capthca.CaptchaConfig;
import io.github.loncra.basic.service.resource.server.config.capthca.EmailCaptchaConfig;
import io.github.loncra.basic.service.resource.server.domain.body.captcha.EmailRequestBody;
import io.github.loncra.framework.captcha.ReceivingTargetSimpleCaptcha;
import io.github.loncra.framework.captcha.SimpleCaptcha;
import io.github.loncra.framework.captcha.token.InterceptToken;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.TimeProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 邮件验证码服务
 *
 * @author maurice
 */
@Component
@RequiredArgsConstructor
public class EmailCaptchaService extends AbstractMessageCaptchaService<EmailRequestBody> {

    public static final String DEFAULT_EMAIL_PARAM_NAME = "emailParamName";

    public static final String DEFAULT_FORMAT_ARGUMENTS_META_KEY = "variables";

    private final EmailCaptchaConfig emailCaptchaConfig;

    private final CaptchaConfig captchaConfig;

    private final Configuration configuration;

    @Override
    protected String getReceivingTargetParamName() {
        return emailCaptchaConfig.getEmailParamName();
    }

    @Override
    protected Map<String, Object> createSendMessageParam(EmailRequestBody entity, DataDictionaryMetadata entry, String captcha) throws Exception {

        Map<String, Object> variables = new LinkedHashMap<>();
        if (Objects.nonNull(entry.getMetadata()) && entry.getMetadata().containsKey(DEFAULT_FORMAT_ARGUMENTS_META_KEY)) {
            Object meta = entry.getMetadata().get(DEFAULT_FORMAT_ARGUMENTS_META_KEY);
            Map<String, Object> metaVariables = CastUtils.convertValue(meta, CastUtils.MAP_TYPE_REFERENCE);
            variables.putAll(metaVariables);
        }
        if (StringUtils.isEmpty(entity.getOperation())) {
            entity.setOperation(entry.getName());
        }

        TimeProperties expireTime = emailCaptchaConfig.getCaptchaExpireTime();

        variables.put(captchaConfig.getCodeVariableName(), captcha);
        variables.put(captchaConfig.getExpireTimeVariableName(), expireTime.getValue() + TimeUnitEnum.valueOf(expireTime.getUnit().toString()).getName());
        variables.put(captchaConfig.getOperationVariableName(), entity.getOperation());

        if (entity.isReplaceOriginalVariables()) {
            variables.putAll(entity.getVariables());
        } else {
            entity.getVariables().forEach(variables::putIfAbsent);
        }

        Template template = new Template(entry.getCode(), entry.getValue().toString(), configuration);
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, variables);

        String title = Objects.toString(variables.get(MessageConstants.DEFAULT_TITLE_KEY), entry.getName());

        return MessageServiceClient.createEmailMessage(Collections.singletonList(entity.getEmail()), title, content, MessageTypeEnum.SYSTEM);
    }

    @Override
    protected SimpleCaptcha createMatchCaptcha(
            String value,
            HttpServletRequest request,
            InterceptToken buildToken,
            EmailRequestBody requestBody
    ) {
        SimpleCaptcha captcha = super.createMatchCaptcha(value, request, buildToken, requestBody);
        ReceivingTargetSimpleCaptcha targetSimpleCaptcha = CastUtils.of(captcha, ReceivingTargetSimpleCaptcha.class);
        targetSimpleCaptcha.setTarget(requestBody.getEmail());

        return targetSimpleCaptcha;
    }

    @Override
    protected String getInterceptorType() {
        return emailCaptchaConfig.getInterceptorType();
    }

    @Override
    protected String generateCaptcha() {
        return RandomStringUtils.secure().nextNumeric(emailCaptchaConfig.getRandomNumericCount());
    }


    @Override
    protected TimeProperties getCaptchaExpireTime() {
        return emailCaptchaConfig.getCaptchaExpireTime();
    }

    @Override
    public String getCaptchaParamName() {
        return emailCaptchaConfig.getCaptchaParamName();
    }

    @Override
    public String getType() {
        return MessageConstants.DEFAULT_EMAIL_TYPE_VALUE;
    }

    @Override
    protected TimeProperties getRetryTime() {
        return emailCaptchaConfig.getRetryTime();
    }

    @Override
    protected Map<String, Object> createGenerateArgs() {
        Map<String, Object> generate = super.createGenerateArgs();

        generate.put(DEFAULT_EMAIL_PARAM_NAME, emailCaptchaConfig.getEmailParamName());
        generate.put(DEFAULT_TYPE_PARAM_NAME, emailCaptchaConfig.getTypeParamName());

        return generate;
    }
}
