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
     * 系统类型
     */
    SYSTEM("SYSTEM", "系统类型"),
    /**
     * 未知类型
     */
    UNKNOWN("UNKNOWN", "未知类型"),
    /**
     * 后台用户数据
     */
    CONSOLE_USER(ResourceSourceEnum.CONSOLE.getValue(), ResourceSourceEnum.CONSOLE.getName()),
    /**
     * 个人用户数据
     */
    PERSONAL_USER(ResourceSourceEnum.PERSONAL.getValue(), ResourceSourceEnum.PERSONAL.getName()),
    ;

    private final String value;

    private final String name;
}
