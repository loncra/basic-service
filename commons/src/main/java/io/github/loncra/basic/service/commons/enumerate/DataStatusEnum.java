package io.github.loncra.basic.service.commons.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据状态枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DataStatusEnum implements NameValueEnum<Integer> {

    /**
     * 新创建
     */
    NEW(10, "新创建"),

    /**
     * 已发布
     */
    RELEASE(20, "已发布"),

    /**
     * 已撤销
     */
    REVOKE(30, "已撤销"),

    ;

    private final Integer value;

    private final String name;
}
