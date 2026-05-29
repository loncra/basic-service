package io.github.loncra.basic.service.resource.server.service.captcha;

import io.github.loncra.basic.service.commons.config.CommonsConfig;
import io.github.loncra.basic.service.message.api.domian.metadata.MessageConstants;
import io.github.loncra.basic.service.message.api.service.MessageServiceClient;
import io.github.loncra.basic.service.resource.api.domain.metadata.DataDictionaryMetadata;
import io.github.loncra.basic.service.resource.api.service.CaptchaServiceClient;
import io.github.loncra.basic.service.resource.server.config.capthca.CaptchaConfig;
import io.github.loncra.basic.service.resource.server.config.capthca.SmsCaptchaConfig;
import io.github.loncra.basic.service.resource.server.domain.body.captcha.SmsRequestBody;
import io.github.loncra.framework.captcha.ReceivingTargetSimpleCaptcha;
import io.github.loncra.framework.captcha.SimpleCaptcha;
import io.github.loncra.framework.captcha.token.InterceptToken;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.exception.SystemException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * 短信验证码服务
 *
 * @author maurice
 */
@Component
@RequiredArgsConstructor
public class SmsCaptchaService extends AbstractMessageCaptchaService<SmsRequestBody> {

    private final SmsCaptchaConfig smsCaptchaConfig;

    private final CaptchaConfig captchaConfig;

    private final CommonsConfig commonsConfig;

    @Override
    protected Map<String, Object> createSendMessageParam(
            SmsRequestBody entity,
            DataDictionaryMetadata entry,
            String captcha
    ) {
        Map<String, Object> metadata = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(entry.getValue().toString(), CastUtils.MAP_TYPE_REFERENCE));

        metadata.put(MessageConstants.VARIABLES_FIELD, Map.of(captchaConfig.getCodeVariableName(), captcha));

        return MessageServiceClient.createSmsMessage(
                Collections.singletonList(entity.getPhoneNumber()),
                commonsConfig.getDefaultSmsChannel(),
                metadata
        );
    }

    @Override
    protected String getInterceptorType() {
        return smsCaptchaConfig.getInterceptorType();
    }

    @Override
    protected SimpleCaptcha createMatchCaptcha(
            String value,
            HttpServletRequest request,
            InterceptToken buildToken,
            SmsRequestBody requestBody
    ) {
        SimpleCaptcha captcha = super.createMatchCaptcha(value, request, buildToken, requestBody);
        ReceivingTargetSimpleCaptcha targetSimpleCaptcha = CastUtils.of(captcha, ReceivingTargetSimpleCaptcha.class);
        targetSimpleCaptcha.setTarget(requestBody.getPhoneNumber());

        return targetSimpleCaptcha;
    }

    @Override
    protected String generateCaptcha() {
        return RandomStringUtils.secure().nextNumeric(smsCaptchaConfig.getRandomNumericCount());
    }

    @Override
    protected TimeProperties getCaptchaExpireTime() {
        return smsCaptchaConfig.getCaptchaExpireTime();
    }

    @Override
    protected TimeProperties getRetryTime() {
        return smsCaptchaConfig.getRetryTime();
    }

    @Override
    public String getType() {
        return MessageConstants.DEFAULT_SMS_TYPE_VALUE;
    }

    @Override
    public String getReceivingTargetParamName() {
        return smsCaptchaConfig.getPhoneNumberParamName();
    }

    @Override
    protected Map<String, Object> createGenerateArgs() {
        Map<String, Object> result = super.createGenerateArgs();

        result.put(CaptchaServiceClient.PHONE_NUMBER_PARAM_NAME, getReceivingTargetParamName());
        return result;
    }

    @Override
    public String getCaptchaParamName() {
        return smsCaptchaConfig.getCaptchaParamName();
    }
}
