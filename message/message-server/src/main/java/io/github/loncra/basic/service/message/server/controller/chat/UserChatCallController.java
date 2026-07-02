package io.github.loncra.basic.service.message.server.controller.chat;

import feign.Param;
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
    ) {
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
        AbstractSocketMessageMetadata<Long> metadata = userChatCallManager.completed(userChatCallId, token);
        return ReturnValueSocketResult.of(List.of(metadata));
    }

    @PutMapping("accept/{userChatCallId:\\d+}")
    public ReturnValueSocketResult<Void> confirm(
            @PathVariable
            Long userChatCallId,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());

        AbstractSocketMessageMetadata<Object> message = userChatCallManager.accept(userChatCallId, token);

        return ReturnValueSocketResult.of(List.of(message));
    }

    @PutMapping("rejected/{userChatCallId:\\d+}")
    public void rejected(
            @PathVariable
            Long userChatCallId,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());

        userChatCallManager.rejected(userChatCallId, token);
    }
}
