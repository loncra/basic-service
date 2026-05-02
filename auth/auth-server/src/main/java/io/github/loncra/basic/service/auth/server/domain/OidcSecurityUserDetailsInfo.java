package io.github.loncra.basic.service.auth.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.loncra.basic.service.auth.server.enumerate.oauth.RegisteredClientScopeEnum;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * spring oauth 的 oidc 用户明细
 *
 * @author maurice.chen
 */
public class OidcSecurityUserDetailsInfo extends OidcUserInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1163307879932770226L;

    public OidcSecurityUserDetailsInfo(Map<String, Object> claims) {
        super(claims);
    }

    @Override
    @JsonIgnore
    public String getProfile() {
        return SystemException.convertSupplier(
                () -> CastUtils.getObjectMapper().writeValueAsString(getClaimAsMap(StandardClaimNames.PROFILE)),
                "CastUtils.getObjectMapper().writeValueAsString error"
        );
    }

    @JsonProperty("profile")
    public Map<String, Object> getProfileMetadata() {
        return getClaimAsMap(StandardClaimNames.PROFILE);
    }

    @Override
    public String getNickName() {
        return getClaimAsString(PrincipalDetailsConstants.NICKNAME_KEY);
    }

    public String getRealName() {
        return getClaimAsString(PrincipalDetailsConstants.REAL_NAME_KEY);
    }

    public String getOpenId() {
        return getClaimAsString(OidcScopes.OPENID);
    }

    public String getUnionId() {
        return getClaimAsString(RegisteredClientScopeEnum.UNIONID.getValue());
    }

    public String getType() {
        return getClaimAsString(TypeIdNameMetadata.TYPE_FIELD_NAME);
    }

    public Map<String, Object> getStatus() {
        return getClaimAsMap(RestResult.DEFAULT_STATUS_NAME);
    }

    public String getRole() {
        return getClaimAsString(RegisteredClientScopeEnum.ROLE.getDescription());
    }
}
