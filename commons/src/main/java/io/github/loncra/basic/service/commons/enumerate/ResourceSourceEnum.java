package io.github.loncra.basic.service.commons.enumerate;

import io.github.loncra.framework.commons.annotation.GetValueStrategy;
import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdNameValueMetadata;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 插件来源枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@GetValueStrategy(type = GetValueStrategy.Type.ToString)
public enum ResourceSourceEnum implements NameValueEnum<String> {

    /**
     * 管理后台用户
     */
    CONSOLE("管理后台用户", ResourceSourceEnum.CONSOLE_SOURCE_VALUE, "^" + ResourceSourceEnum.CONSOLE_SOURCE_VALUE + "|" + ResourceSourceEnum.CONSOLE_SOURCE_VALUE + ":\\d+$", new IdNameValueMetadata<>("ADMIN", "超高级管理员", "admin")),

    /**
     * 个人用户
     */
    PERSONAL("个人用户", ResourceSourceEnum.PERSONAL_SOURCE_VALUE, "^" + ResourceSourceEnum.PERSONAL_SOURCE_VALUE + "|" + ResourceSourceEnum.PERSONAL_SOURCE_VALUE + ":\\d+$", new IdNameValueMetadata<>("PERSONAL", "个人用户", "personal")),

    ;

    /**
     * 中文名称
     */
    private final String name;

    /**
     * 值
     */
    private final String value;

    /**
     * 用户类型的正则表达式校验
     */
    private final String regex;

    /**
     * 管理员角色信息
     */
    private final IdNameValueMetadata<String, String> adminAuthority;

    /**
     * 管理后台用户应用来源值
     */
    public static final String CONSOLE_SOURCE_VALUE = "CONSOLE";

    public static final String PERSONAL_SOURCE_VALUE = "PERSONAL";

    public static ResourceSourceEnum parse(String value) {
        for (ResourceSourceEnum resourceSource : ResourceSourceEnum.values()) {
            if (resourceSource.getValue().contains(value)) {
                return resourceSource;
            }
        }
        throw new SystemException("找不到 [" + value + "] 对应的 ResourceSourceEnum 枚举实例");
    }

    /**
     * 校验值是否规范，正确的规范为 资源类型:id:登录账号
     *
     * @param value 值
     *
     * @return true 是，否则 false
     */
    public static boolean validate(String value) {
        return Arrays.stream(ResourceSourceEnum.values())
                .anyMatch(v -> validate(v, value));
    }

    public static boolean validate(
            ResourceSourceEnum source,
            String value
    ) {
        return Pattern.matches(source.getRegex(), value);
    }

    public static boolean validate(
            List<ResourceSourceEnum> sources,
            String value
    ) {
        return sources.stream()
                .anyMatch(s -> Pattern.matches(s.getRegex(), value));
    }

}
