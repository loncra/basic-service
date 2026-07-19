package io.github.loncra.basic.service.monolith.rabbit;

import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.tenant.SimpleTenantContext;
import io.github.loncra.framework.commons.tenant.holder.TenantContextHolder;
import io.github.loncra.framework.spring.security.core.authentication.AccessTokenContextRepository;
import io.github.loncra.framework.spring.security.core.authentication.TenantContextSecurityFilter;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.support.AccessTokenAuditAuthenticationSuccessDetails;
import io.github.loncra.framework.spring.web.device.DeviceUtils;
import io.github.loncra.framework.spring.web.mvc.SpringMvcUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.config.ContainerCustomizer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SpringSecurityRabbitTemplateCustomizer implements RabbitTemplateCustomizer, ContainerCustomizer<SimpleMessageListenerContainer> {

    private final AccessTokenContextRepository accessTokenContextRepository;

    @Override
    public void customize(RabbitTemplate rabbitTemplate) {
        rabbitTemplate.addBeforePublishPostProcessors(message -> {
            Optional<HttpServletRequest> optional = SpringMvcUtils.getHttpServletRequest();
            if (optional.isPresent()) {
                HttpServletRequest httpServletRequest = optional.get();
                String accessToken = accessTokenContextRepository.getAccessToken(httpServletRequest);
                if (StringUtils.isNotEmpty(accessToken)) {
                    message.getMessageProperties()
                            .setHeader(accessTokenContextRepository.getAuthenticationProperties().getAccessToken().getHeaderName(), accessToken);
                }
                String deviceIdentified = httpServletRequest.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);
                if (StringUtils.isNotEmpty(deviceIdentified)) {
                    message.getMessageProperties()
                            .setHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME, deviceIdentified);
                }
            } else {
                String accessToken = getFromSecurityContext(SecurityContextHolder.getContext());
                if (StringUtils.isNotEmpty(accessToken)) {
                    message.getMessageProperties()
                            .setHeader(accessTokenContextRepository.getAuthenticationProperties().getAccessToken().getHeaderName(), accessToken);
                }
            }

            return message;
        });
        rabbitTemplate.addAfterReceivePostProcessors(message -> {
            String accessToken = message.getMessageProperties().getHeader(accessTokenContextRepository.getAuthenticationProperties().getAccessToken().getHeaderName());

            SecurityContext securityContext = accessTokenContextRepository.getSecurityContext(accessToken);
            if (Objects.nonNull(securityContext)) {
                SecurityContextHolder.setContext(securityContext);
            }
            return message;
        });
    }

    private String getFromSecurityContext(SecurityContext securityContext) {
        if (Objects.isNull(securityContext.getAuthentication())) {
            return null;
        }

        if (!AuditAuthenticationToken.class.isAssignableFrom(securityContext.getAuthentication().getClass())){
            return null;
        }

        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        if (token.getDetails() instanceof AccessTokenAuditAuthenticationSuccessDetails details) {
            return details.getToken().getValue();
        }

        return null;
    }

    @Override
    public void configure(SimpleMessageListenerContainer container) {
        container.addAfterReceivePostProcessors(message -> {
            Object accessToken = message.getMessageProperties().getHeader(accessTokenContextRepository.getAuthenticationProperties().getAccessToken().getHeaderName());
            if (Objects.isNull(accessToken)) {
                return message;
            }
            SecurityContext securityContext = accessTokenContextRepository.getSecurityContext(Objects.toString(accessToken));
            if (Objects.nonNull(securityContext) && Optional.ofNullable(securityContext.getAuthentication()).isPresent()) {
                SecurityContextHolder.setContext(securityContext);
            }
            SimpleTenantContext tenantContext = TenantContextSecurityFilter.resolveTenantContext(securityContext);
            TenantContextHolder.set(tenantContext);
            return message;
        });
    }
}
