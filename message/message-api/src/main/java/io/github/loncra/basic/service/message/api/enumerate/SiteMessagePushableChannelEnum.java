package io.github.loncra.basic.service.message.api.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 站内信消息推送渠道
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SiteMessagePushableChannelEnum implements NameValueEnum<Integer> {

    /**
     * 微信小程序
     */
    WECHAT_APPLET(10, "微信小程序"),

    /**
     * 微信公众号
     */
    WECHAT_OFFICIAL(10, "微信公众号"),

    /**
     * 警告
     */
    APP(10, "app 应用"),

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
