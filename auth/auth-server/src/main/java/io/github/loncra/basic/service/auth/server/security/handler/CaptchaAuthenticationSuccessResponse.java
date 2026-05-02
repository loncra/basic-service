package io.github.loncra.basic.service.auth.server.security.handler;

import io.github.loncra.basic.service.auth.server.domain.AuthenticationInfo;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.domain.metadata.address.IpRegionMetadata;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.spring.security.core.authentication.handler.JsonAuthenticationSuccessResponse;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.web.device.DeviceUtils;
import io.github.loncra.framework.spring.web.mvc.SpringMvcUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * json 形式的认证失败具柄实现
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CaptchaAuthenticationSuccessResponse implements JsonAuthenticationSuccessResponse {

    private final CaptchaAuthenticationFailureResponse jsonAuthenticationFailureHandler;

    private final AmqpTemplate amqpTemplate;

    @Override
    public void setting(
            RestResult<Object> result,
            HttpServletRequest request
    ) {

        /*String traceId = request.getHeader(TLogConstants.TLOG_TRACE_KEY);
        if (StringUtils.isNotEmpty(traceId)) {
            result.getMeta().put(SystemConstants.TRACE_ID_FIELD_NAME, traceId);
        }*/

        Object token = result.getData();
        if (!AuditAuthenticationToken.class.isAssignableFrom(token.getClass())) {
            return;
        }

        AuditAuthenticationToken authenticationToken = CastUtils.cast(token);
        if (authenticationToken.isRememberMe()) {
            return;
        }

        jsonAuthenticationFailureHandler.deleteAllowableFailureNumber(request);

        UserAgent device = DeviceUtils.getRequiredCurrentDevice(request);
        String ip = SpringMvcUtils.getIpAddress(request);

        Map<String, Object> details = CastUtils.convertValue(authenticationToken.getDetails(), CastUtils.MAP_TYPE_REFERENCE);
        details.put(PrincipalDetailsConstants.USERNAME_KEY, authenticationToken.getSecurityPrincipal().getUsername());
        AuthenticationInfo info = new AuthenticationInfo();
        info.setDevice(device.toMap());
        info.setMeta(details);
        info.setIpRegionMeta(IpRegionMetadata.of(ip));
        info.setPrincipal(authenticationToken.getName());

        /*amqpTemplate.convertAndSend(
                SystemConstants.SYS_AUTHENTICATION_RABBITMQ_EXCHANGE,
                AuthenticationInfoConsumer.DEFAULT_QUEUE_NAME,
                SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(info))
        );*/
    }

}
