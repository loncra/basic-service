package io.github.loncra.basic.service.message.server.resolver.support.sms;

import io.github.loncra.basic.service.commons.enumerate.CloudChannelEnum;
import io.github.loncra.basic.service.message.api.domian.metadata.SmsBalanceMetadata;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsMessageBody;
import io.github.loncra.basic.service.message.server.domain.entity.SmsMessageEntity;
import io.github.loncra.framework.commons.RestResult;

import java.util.Map;

/**
 * 短信渠道发送者
 *
 * @author maurice
 */
public interface SmsChannelSender {

    /**
     * 获取短信渠道类型
     *
     * @return 短信渠道类型
     */
    CloudChannelEnum getType();

    /**
     * 发送短信
     *
     * @param entity 短信实体
     * @return rest 结果集
     */
    RestResult<Map<String, Object>> sendSms(SmsMessageEntity entity);

    /**
     * 获取可用余额
     *
     * @return 可用余额
     */
    SmsBalanceMetadata getBalance();

    /**
     * 获取内容明细
     *
     * @param body 请求体
     *
     * @return 内容明细
     */
    default SmsMessageBody createSmsMessageEntity(SmsMessageBody body) {
        return body;
    }
}
