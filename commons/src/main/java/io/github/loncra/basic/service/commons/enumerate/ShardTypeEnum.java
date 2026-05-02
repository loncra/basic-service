package io.github.loncra.basic.service.commons.enumerate;

import io.github.loncra.basic.service.commons.resolver.soupprt.WeChatAppletShardResolver;
import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分享类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShardTypeEnum implements NameValueEnum<String> {

    /**
     * 微信小程序
     */
    WE_CHAT_APPLE(WeChatAppletShardResolver.DEFAULT_TYPE, "微信小程序");

    private final String value;

    private final String name;
}
