package io.github.loncra.basic.service.message.api.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MessageTypeEnum implements NameValueEnum<Integer> {

    /**
     * 通知
     */
    NOTICE(10, "通知消息"),

    /**
     * 警告
     */
    WARNING(20, "警告消息"),

    /**
     * 系统
     */
    SYSTEM(30, "系统消息"),

    /**
     * 验证码
     */
    CAPTCHA(40, "验证码"),

    /**
     * 推广
     */
    PROMOTION(50, "推广消息"),

    /**
     * 即时聊天
     */
    IM(60, "即时聊天"),

    /**
     * 未知
     */
    UNKNOWN(99,"未知"),
    ;

    /**
     * 值
     */
    private final Integer value;

    /**
     * 名称
     */
    private final String name;

}
