package io.github.loncra.basic.service.resource.api.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 附件类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AttachmentTypeEnum implements NameValueEnum<String> {

    /**
     * 用户附件
     */
    USER_FILE("user.file", "用户资源附件"),

    /**
     * 系统文件
     */
    SYSTEM_FILE("system.file", "系统文件"),

    /**
     * 临时文件
     */
    TEMP("temp", "临时文件附件"),

    /**
     * 头像附件
     */
    AVATAR("avatar", "头像附件");

    private final String value;

    private final String name;
}
