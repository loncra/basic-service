package io.github.loncra.basic.service.auth.server.enumerate.oauth;

import io.github.loncra.framework.commons.enumerate.NameEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms;

/**
 * mac 算法枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MacAlgorithmEnum implements NameEnum, JwsAlgorithm {

    /**
     * HMAC using SHA-256 (Required)
     */
    HS256(JwsAlgorithms.HS256),

    /**
     * HMAC using SHA-384 (Optional)
     */
    HS384(JwsAlgorithms.HS384),

    /**
     * HMAC using SHA-512 (Optional)
     */
    HS512(JwsAlgorithms.HS512);

    private final String name;
}
