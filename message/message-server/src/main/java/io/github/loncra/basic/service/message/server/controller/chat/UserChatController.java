package io.github.loncra.basic.service.message.server.controller.chat;

import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatConversationEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatMessageEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatRoomEntity;
import io.github.loncra.basic.service.message.server.service.chat.UserChatManager;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.HttpRequestParameterMapUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.idempotent.annotation.Idempotent;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.socketio.api.SocketResult;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 即时聊天管理
 *
 * @author maurice.chen
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("user/chat")
@Plugin(
        name = "我的聊天",
        id = "my_chat_message",
        parent = "my_message",
        authority = "isFullyAuthenticated()",
        type = ResourceTypeEnum.NAVIGATION_DATA_TYPE,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class UserChatController {

    private final UserChatManager userChatManager;

    @PreAuthorize("isFullyAuthenticated()")
    @PutMapping("/send/{chatRoomId:\\d+}")
    public SocketResult send(
            @PathVariable Long chatRoomId,
            @RequestBody List<Map<String, Object>> message,
            @CurrentSecurityContext SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return userChatManager.send(chatRoomId, message, token);
    }

    @PostMapping
    @PreAuthorize("isFullyAuthenticated()")
    public List<UserChatConversationEntity> my(
            @CurrentSecurityContext SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return userChatManager.my(token);
    }

    @PostMapping("message/histories/{chatRoomId:\\d+}")
    @PreAuthorize("isAuthenticated()")
    public Page<UserChatMessageEntity> histories(
            PageRequest pageRequest,
            @PathVariable Long chatRoomId,
            @CurrentSecurityContext SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return userChatManager.histories(pageRequest, chatRoomId, token);
    }

    @PostMapping("message/read")
    @PreAuthorize("isAuthenticated()")
    public SocketResult read(
            @RequestParam List<Long> messageIds,
            @CurrentSecurityContext SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return userChatManager.readMessage(messageIds, token);
    }

    @PutMapping("/conversation/create")
    @PreAuthorize("isAuthenticated()")
    public SocketResult createConversation(
            @Valid
            @RequestBody
            UserChatRoomEntity userChatRoomEntity,
            @RequestParam List<String> principals,
            @CurrentSecurityContext SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return userChatManager.createConversation(userChatRoomEntity, token, principals);
    }
}
