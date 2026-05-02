package io.github.loncra.basic.service.commons.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 导入导出类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ImportExportTypeEnum implements NameValueEnum<String> {
    /**
     * 未知类型
     */
    SYSTEM("UNKNOWN", "系统类型"),
    /**
     * 未知类型
     */
    UNKNOWN("UNKNOWN", "未知类型")
    ;

    private final String value;

    private final String name;
}
