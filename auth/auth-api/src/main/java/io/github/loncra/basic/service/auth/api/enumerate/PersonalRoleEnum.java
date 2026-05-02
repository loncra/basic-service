package io.github.loncra.basic.service.auth.api.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import io.github.loncra.framework.security.entity.RoleAuthority;
import io.github.loncra.framework.spring.security.core.plugin.metadata.IdRoleAuthorityMetadata;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 租赁角色枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PersonalRoleEnum implements NameValueEnum<String> {

    /**
     * 视频制作
     */
    VIDEO_ASSEMBLY("VIDEO_ASSEMBLY", "视频制作"),

    ;

    private final String value;

    private final String name;

    public static IdRoleAuthorityMetadata getRoleAuthority(PersonalRoleEnum role) {
        return new IdRoleAuthorityMetadata(new RoleAuthority(role.getName(), role.getValue()));
    }
}
