package io.github.loncra.basic.service.auth.server.domain;

import io.github.loncra.basic.service.commons.domain.metadata.address.IpRegionMetadata;
import io.github.loncra.framework.commons.id.StringIdEntity;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Map;

/**
 * 认证信息
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AuthenticationInfo extends StringIdEntity {

    @Serial
    private static final long serialVersionUID = 8448949374931042267L;

    /**
     * 元数据信息
     */
    private Map<String, Object> meta;

    /**
     * ip 地址
     */
    @NotEmpty
    @EqualsAndHashCode.Exclude
    private IpRegionMetadata ipRegionMeta;

    /**
     * 设备名称
     */
    @NotEmpty
    @EqualsAndHashCode.Exclude
    private Map<String, String> device;

    /**
     * 用户信息
     */
    private String principal;

    /**
     * 备注
     */
    @EqualsAndHashCode.Exclude
    private String remark;
}
