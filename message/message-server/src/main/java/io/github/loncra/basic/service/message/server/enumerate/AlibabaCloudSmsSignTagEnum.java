package io.github.loncra.basic.service.message.server.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 阿里云短信签名标签枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AlibabaCloudSmsSignTagEnum implements NameValueEnum<String> {

    /**
     * 用户自定义创建签名
     */
    CUSTOM_CREATION("2","用户自定义创建签名"),
    /**
     * 系统赠送签名
     */
    SYSTEM_GIFT("3","系统赠送签名"),
    /**
     * 测试签名
     */
    TEST("4","测试签名"),
    /**
     * 试用签名
     */
    TRIAL("5","试用签名"),

    ;


    private final String value;

    private final String name;
}
