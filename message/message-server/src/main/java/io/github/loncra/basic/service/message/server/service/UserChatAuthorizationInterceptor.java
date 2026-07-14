package io.github.loncra.basic.service.message.server.service;

import com.corundumstudio.socketio.SocketIOClient;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatParticipantEntity;
import io.github.loncra.basic.service.message.server.service.chat.UserChatParticipantService;
import io.github.loncra.framework.socketio.core.interceptor.AuthorizationInterceptor;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
        List<UserChatParticipantEntity> chatParticipants = userChatParticipantService.findByPrincipal(socketAuthenticationToken.getName());
        Set<String> ids = chatParticipants.stream()
                .map(UserChatParticipantEntity::getUserChatRoomId)
                .map(Objects::toString)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        client.joinRooms(ids);
    }
}
