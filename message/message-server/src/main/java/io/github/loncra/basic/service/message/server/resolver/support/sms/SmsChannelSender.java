package io.github.loncra.basic.service.message.server.resolver.support.sms;

import io.github.loncra.basic.service.commons.enumerate.CloudChannelEnum;
import io.github.loncra.basic.service.message.api.domian.metadata.SmsBalanceMetadata;
import io.github.loncra.basic.service.message.api.domian.metadata.SmsSignMetadata;
import io.github.loncra.basic.service.message.api.domian.metadata.SmsTemplateMetadata;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsMessageBody;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsSignResponseBody;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsTemplateResponseBody;
import io.github.loncra.basic.service.message.server.domain.entity.SmsMessageEntity;
import io.github.loncra.framework.commons.RestResult;

import java.util.List;
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
     * 获取签名列表
     *
     * @return 签名列表
     */
    List<SmsSignResponseBody> signList();

    /**
     * 创建签名
     *
     * @param metadata 短信签名元数据信息
     *
     * @return 执行结果
     */
    SmsSignResponseBody saveSign(SmsSignMetadata metadata);

    /**
     * 删除签名
     *
     * @param ids 唯一识别集合
     *
     */
    void deleteSign(List<String> ids);

    /**
     * 获取签名信息
     *
     * @param id 逐渐 id
     *
     * @return 短信签名响应体
     */
    SmsSignResponseBody getSign(String id);

    /**
     * 获取短信模版集合
     *
     * @return 短信模版集合
     */
    List<SmsTemplateResponseBody> templateList();

    /**
     * 获取模本信息
     *
     * @param id 模版 id
     *
     * @return 短信模版
     */
    SmsTemplateResponseBody getTemplate(String id);

    /**
     * 删除模版
     *
     * @param ids 主键 id 集合
     */
    void deleteTemplate(List<String> ids);

    /**
     * 保存模版
     *
     * @param metadata 元数据信息
     *
     * @return 模版响应体
     */
    SmsTemplateResponseBody saveTemplate(SmsTemplateMetadata metadata);

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
