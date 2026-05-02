package io.github.loncra.basic.service.auth.server.enumerate.oauth;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;

/**
 * 签名算法枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SignatureAlgorithmEnum implements NameValueEnum<SignatureAlgorithm>, JwsAlgorithm {

    /**
     * RSASSA-PKCS1-v1_5 using SHA-256 (Recommended)
     */
    RS256(SignatureAlgorithm.RS256, JwsAlgorithms.RS256),

    /**
     * RSASSA-PKCS1-v1_5 using SHA-384 (Optional)
     */
    RS384(SignatureAlgorithm.RS384, JwsAlgorithms.RS384),

    /**
     * RSASSA-PKCS1-v1_5 using SHA-512 (Optional)
     */
    RS512(SignatureAlgorithm.RS512, JwsAlgorithms.RS512),

    /**
     * ECDSA using P-256 and SHA-256 (Recommended+)
     */
    ES256(SignatureAlgorithm.ES256, JwsAlgorithms.ES256),

    /**
     * ECDSA using P-384 and SHA-384 (Optional)
     */
    ES384(SignatureAlgorithm.ES384, JwsAlgorithms.ES384),

    /**
     * ECDSA using P-521 and SHA-512 (Optional)
     */
    ES512(SignatureAlgorithm.ES512, JwsAlgorithms.ES512),

    /**
     * RSASSA-PSS using SHA-256 and MGF1 with SHA-256 (Optional)
     */
    PS256(SignatureAlgorithm.PS256, JwsAlgorithms.PS256),

    /**
     * RSASSA-PSS using SHA-384 and MGF1 with SHA-384 (Optional)
     */
    PS384(SignatureAlgorithm.PS384, JwsAlgorithms.PS384),

    /**
     * RSASSA-PSS using SHA-512 and MGF1 with SHA-512 (Optional)
     */
    PS512(SignatureAlgorithm.PS512, JwsAlgorithms.PS512);

    private final SignatureAlgorithm value;

    private final String name;
}
