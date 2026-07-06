package io.github.loncra.basic.service.message.server.controller.chat;

import feign.Param;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallParticipantEntity;
import io.github.loncra.basic.service.message.server.enumerate.chat.call.UserChatCallTypeEnum;
import io.github.loncra.basic.service.message.server.service.chat.call.UserChatCallManager;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.socketio.api.ReturnValueSocketResult;
import io.github.loncra.framework.socketio.api.SocketResult;
import io.github.loncra.framework.socketio.api.metadata.AbstractSocketMessageMetadata;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("user/chat/call")
public class UserChatCallController {

    private final UserChatCallManager userChatCallManager;

    @PostMapping("/{type:\\d+}/{userChatRoomId:\\d+}")
    public SocketResult create(
            @PathVariable
            Integer type,
            @PathVariable
            Long userChatRoomId,
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestParam
            List<String> callingPrincipals
    ) throws IOException {
        UserChatCallTypeEnum typeEnum = ValueEnum.ofEnum(UserChatCallTypeEnum.class, type);
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return userChatCallManager.create(typeEnum, userChatRoomId, token, callingPrincipals);
    }

    @DeleteMapping
    public SocketResult completed(
            @Param
            Long userChatCallId,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        List<AbstractSocketMessageMetadata<Object>> messages = userChatCallManager.completed(userChatCallId, token);
        return ReturnValueSocketResult.of(new LinkedList<>(messages));
    }

    @PutMapping("accept/{userChatCallId:\\d+}")
    public ReturnValueSocketResult<UserChatCallParticipantEntity> accept(
            @PathVariable
            Long userChatCallId,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return userChatCallManager.accept(userChatCallId, token);
    }

    @PutMapping("rejected/{userChatCallId:\\d+}")
    public ReturnValueSocketResult<Void> rejected(
            @PathVariable
            Long userChatCallId,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        List<AbstractSocketMessageMetadata<Object>> messages = userChatCallManager.rejected(userChatCallId, token);
        return ReturnValueSocketResult.of(new LinkedList<>(messages));
    }

    @GetMapping("/{userChatCallId:\\d+}")
    public UserChatCallEntity get(
            @PathVariable
            Long userChatCallId,
            @RequestParam(required = false, defaultValue = "false")
            boolean responseBody,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        UserChatCallEntity entity = userChatCallManager.getUserChatCall(userChatCallId, token);

        if (responseBody) {
            return userChatCallManager.convertUserChatCallEntityToResponseBody(entity);
        }

        return entity;
    }
}
