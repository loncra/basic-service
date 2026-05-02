package io.github.loncra.basic.service.resource.server.service.captcha;

import io.github.loncra.basic.service.message.api.service.MessageServiceClient;
import io.github.loncra.basic.service.resource.api.domain.metadata.DataDictionaryMetadata;
import io.github.loncra.basic.service.resource.server.service.dictionary.DataDictionaryService;
import io.github.loncra.framework.captcha.*;
import io.github.loncra.framework.captcha.intercept.Interceptor;
import io.github.loncra.framework.captcha.storage.CaptchaStorageManager;
import io.github.loncra.framework.captcha.token.InterceptToken;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.id.IdEntity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.Assert;
import org.springframework.validation.Validator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 消息验证码发送的抽象实现
 *
 * @param <T> 消息类型实现
 * @author maurice
 */
@Slf4j
public abstract class AbstractMessageCaptchaService<T extends MessageCaptchaType> extends AbstractCaptchaService<T> {

    public static final String DEFAULT_CODE_LENGTH = "codeLength";

    public static final String DEFAULT_TYPE_PARAM_NAME = "typeParamName";

    /**
     * 数据字典服务
     */
    @Getter
    @Setter(onMethod_ =  @Autowired)
    private DataDictionaryService dataDictionaryService;

    /**
     * 消息服务
     */
    @Getter
    @Setter(onMethod_ =  @Autowired)
    private MessageServiceClient messageServiceClient;

    @Lazy
    @Override
    @Autowired
    public void setInterceptor(Interceptor interceptor) {
        super.setInterceptor(interceptor);
    }

    @Override
    @Autowired
    @Qualifier("mvcValidator")
    public void setValidator(Validator validator) {
        super.setValidator(validator);
    }

    @Override
    @Autowired
    public void setCaptchaProperties(CaptchaProperties captchaProperties) {
        super.setCaptchaProperties(captchaProperties);
    }

    @Override
    @Autowired
    public void setCaptchaStorageManager(CaptchaStorageManager captchaStorageManager) {
        super.setCaptchaStorageManager(captchaStorageManager);
    }

    @Override
    protected GenerateCaptchaResult doGenerateCaptcha(InterceptToken buildToken, T requestBody, HttpServletRequest request) throws Exception {
        List<DataDictionaryMetadata> dicList = dataDictionaryService.findDataDictionaryMetas(requestBody.getMessageType());

        Assert.isTrue(CollectionUtils.isNotEmpty(dicList), "找不到类型为:" + requestBody.getMessageType() + "的消息模板");
        Assert.isTrue(dicList.size() == 1, "通过:" + requestBody.getMessageType() + "找出" + dicList.size() + "条记录，并非一条记录");

        String captcha = generateCaptcha();

        DataDictionaryMetadata entry = dicList.getFirst();

        Map<String, Object> param = createSendMessageParam(requestBody, entry, captcha);

        RestResult<Object> result = messageServiceClient.send(param);
        // 如果发送成记录短信验证码到 redis 中给校验备用。
        Assert.isTrue(result.isSuccess(), result.getMessage());
        return onSendSuccess(GenerateCaptchaResult.of(result, captcha));

    }

    protected GenerateCaptchaResult onSendSuccess(GenerateCaptchaResult result) {
        RestResult<Map<String, Object>> restResult = createCodeLengthResult(result, getRetryTime());
        return GenerateCaptchaResult.of(restResult, result.getMatchValue());
    }

    public static RestResult<Map<String, Object>> createCodeLengthResult(GenerateCaptchaResult result, TimeProperties retryTime) {
        RestResult<Map<String, Object>> restResult = RestResult.ofSuccess(new LinkedHashMap<>());
        RestResult<Object> superResult = CastUtils.cast(result.getResult());
        restResult.getData().put(IdEntity.ID_FIELD_NAME, superResult.getData());
        if (Objects.nonNull(retryTime)) {
            restResult.getData().put(Expired.class.getSimpleName().toLowerCase(), retryTime);
        }
        restResult.getData().put(DEFAULT_CODE_LENGTH, result.getMatchValue().length());

        return restResult;
    }

    @Override
    protected boolean matchesCaptcha(HttpServletRequest request, SimpleCaptcha captcha) {

        if (captcha instanceof ReceivingTargetSimpleCaptcha) {
            ReceivingTargetSimpleCaptcha exist = CastUtils.cast(captcha);
            String target = exist.getTarget();
            String requestUsername = request.getParameter(getReceivingTargetParamName());

            if (!Strings.CS.equals(requestUsername, target)) {
                return false;
            }
        }
        return super.matchesCaptcha(request, captcha);
    }

    @Override
    protected boolean isMatchesFailureDeleteCaptcha() {
        return false;
    }

    /**
     * 获取收信目标参数名称
     *
     * @return 收信目标参数名称
     */
    protected abstract String getReceivingTargetParamName();


    /**
     * 创建消息发送参数
     *
     * @param entity  泛型实体
     * @param entry   字典内容
     * @param captcha 验证码
     * @return 参数 map
     */
    protected abstract Map<String, Object> createSendMessageParam(T entity, DataDictionaryMetadata entry, String captcha) throws Exception;

    /**
     * 生成验证码
     *
     * @return 验证码
     */
    protected abstract String generateCaptcha();

}
