package io.github.loncra.basic.service.auth.server.security.handler;

import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.spring.security.core.authentication.handler.JsonAuthenticationSuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationSuccessMqNotification implements JsonAuthenticationSuccessResponse {

    private final AmqpTemplate amqpTemplate;

    @Override
    public void setting(
            RestResult<Object> result,
            HttpServletRequest request
    ) {
        amqpTemplate.convertAndSend(
                SystemConstants.USER_AUTH_SUCCESS_FANOUT_EXCHANGE,
                StringUtils.EMPTY,
                SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(result.getData()))
        );
    }
}
