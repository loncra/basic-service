package io.github.loncra.basic.service.commons.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 云渠道枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum CloudChannelEnum implements NameValueEnum<String> {

    /**
     * 阿里云
     */
    ALIBABA_CLOUD("阿里云", "alibabaCloud"),
    ;

    private final String name;

    private final String value;
}
