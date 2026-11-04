package io.github.loncra.basic.service.message.server.service;

import com.corundumstudio.socketio.SocketIOClient;
import io.github.loncra.basic.service.message.api.domian.metadata.SocketSessionMetadata;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatParticipantEntity;
import io.github.loncra.basic.service.message.server.service.chat.UserChatParticipantService;
import io.github.loncra.framework.socketio.core.interceptor.AuthorizationInterceptor;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AuditAuthenticationSuccessDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserChatAuthorizationInterceptor implements AuthorizationInterceptor {

    private final UserChatParticipantService userChatParticipantService;

    @Override
    public boolean isSupport(SecurityContext securityContext) {
        return true;
    }

    @Override
    public void onDisconnect(SocketIOClient client) {
        AuthorizationInterceptor.super.onDisconnect(client);
        client.leaveRooms(client.getAllRooms());
    }

    @Override
    public void onConnect(
            SocketIOClient client,
            AuditAuthenticationToken socketAuthenticationToken
    ) {
        AuthorizationInterceptor.super.onConnect(client, socketAuthenticationToken);
        registerExecuteShell(client, socketAuthenticationToken);
        List<UserChatParticipantEntity> chatParticipants = userChatParticipantService.findByPrincipal(socketAuthenticationToken.getName());
        Set<String> ids = chatParticipants.stream()
                .map(UserChatParticipantEntity::getUserChatRoomId)
                .map(Objects::toString)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        client.joinRooms(ids);
    }

    private void registerExecuteShell(SocketIOClient client, AuditAuthenticationToken token) {
        String executeShell = client.getHandshakeData().getSingleUrlParam(SocketSessionMetadata.EXECUTE_SHELL_METADATA_NAME);
        if (StringUtils.isBlank(executeShell)) {
            return;
        }

        if (token.getDetails() instanceof AuditAuthenticationSuccessDetails details) {
            details.getMetadata().put(SocketSessionMetadata.EXECUTE_SHELL_METADATA_NAME, executeShell);
        }
    }
}
