package io.github.loncra.basic.service.auth.api.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import io.github.loncra.framework.security.plugin.PluginInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ResourceTypeEnum  implements NameValueEnum<String> {

    MENU(ResourceTypeEnum.RESOURCE_MENU_TYPE, "菜单类型"),

    ROOT(ResourceTypeEnum.RESOURCE_ROOT_TYPE, "菜单根节点类型"),

    DIRECTORY(ResourceTypeEnum.RESOURCE_DIRECTORY_TYPE, "菜单分组类型"),

    TOOL(ResourceTypeEnum.RESOURCE_TOOL_TYPE, "工具栏类型"),

    SECURITY(ResourceTypeEnum.SECURITY_TYPE,"安全类型"),

    PROFILE(ResourceTypeEnum.RESOURCE_PROFILE_TYPE, "个人设置类型")

    ;

    private final String value;

    private final String name;


    public static final String RESOURCE_MENU_TYPE = "menu";

    public static final String RESOURCE_ROOT_TYPE = "root";

    public static final String RESOURCE_DIRECTORY_TYPE = "directory";

    public static final String RESOURCE_TOOL_TYPE = "tool";

    public static final String RESOURCE_PROFILE_TYPE = "profile";

    public static final String SECURITY_TYPE = PluginInfo.DEFAULT_TYPE_VALUE;
}