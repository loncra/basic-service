package io.github.loncra.basic.service.monolith.rabbit;

import io.github.loncra.framework.spring.security.core.authentication.AccessTokenContextRepository;
import io.github.loncra.framework.spring.web.device.DeviceUtils;
import io.github.loncra.framework.spring.web.mvc.SpringMvcUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.config.ContainerCustomizer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.security.core.Authentication;
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
            if (optional.isEmpty()) {
                return message;
            }
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

    @Override
    public void configure(SimpleMessageListenerContainer container) {
        container.addAfterReceivePostProcessors(message -> {
            Object accessToken = message.getMessageProperties().getHeader(accessTokenContextRepository.getAuthenticationProperties().getAccessToken().getHeaderName());
            if (Objects.isNull(accessToken)) {
                return message;
            }
            SecurityContext securityContext = accessTokenContextRepository.getSecurityContext(Objects.toString(accessToken));
            if (Objects.nonNull(securityContext) && Optional.of(securityContext.getAuthentication()).map(Authentication::isAuthenticated).get()) {
                SecurityContextHolder.setContext(securityContext);
            }
            return message;
        });
    }
}
