package io.github.loncra.basic.service.auth.api.domain;

import io.github.loncra.framework.wechat.domain.WechatUserDetails;
import lombok.Data;

import java.io.Serial;
import java.util.Map;

/**
 * 抽象的微信认证
 *
 * @author maurice.chen
 */
@Data
public abstract class AbstractWechatAuthentication implements WechatUserDetails {

    @Serial
    private static final long serialVersionUID = -8657939301351629315L;

    /**
     * 认证用户
     */
    private String principal;

    /**
     * session key
     */
    private String sessionKey;

    /**
     * open id
     */
    private String openId;

    /**
     * union id
     */
    private String unionId;

    /**
     * 获取元数据信息
     *
     * @return 元数据信息
     */
    public abstract Map<String, Object> getMetadata();
}
