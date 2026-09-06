package io.github.loncra.basic.service.auth.api.domain;

import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.annotation.Description;
import io.github.loncra.framework.commons.enumerate.security.UserStatus;
import io.github.loncra.framework.commons.id.number.LongIdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * 系统用户信息
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractBasicSystemUser extends LongIdEntity {

    @Serial
    private static final long serialVersionUID = 6199203924933850902L;

    @Description(value = "登录账户", sort = 2)
    private String username;

    /**
     * 状态:1.启用、2.禁用、3.锁定
     */
    @Description(value = "状态", sort = 3)
    private UserStatus status;

    /**
     * 获取登录密码
     *
     * @return 登录密码
     */
    public abstract String getPassword();

    /**
     * 获取用户角色
     *
     * @return 角色信息
     */
    public abstract Set<Long> getRoleIds();

    /**
     * 设置角色信息
     *
     * @param roleIds 角色 id 集合
     */
    public abstract void setRoleIds(Set<Long> roleIds);

    /**
     * 获取资源信息
     *
     * @return 资源信息
     */
    public abstract Set<Long> getResourceIds();

    /**
     * 设置资源信息
     *
     * @param resourceIds 资源 id 集合
     */
    public abstract void setResourceIds(Set<Long> resourceIds);

    /**
     * 转换当事人元数据信息
     *
     * @return 元数据信息 map
     */
    public abstract Map<String, Object> toPrincipalMetadata();

    /**
     * 获取用户类型
     *
     * @return 用户类型
     */
    public abstract ResourceSourceEnum getType();

    public abstract Instant getLastAuthenticationTime();

    /**
     * 获取系统名称
     *
     * @return 系统名称
     */
    public String getSystemName() {
        return getType() + CacheProperties.DEFAULT_SEPARATOR + getId();
    }
}
