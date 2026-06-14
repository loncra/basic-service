package io.github.loncra.basic.service.message.server.controller.chat;

import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatMessageEntity;
import io.github.loncra.basic.service.message.server.service.chat.UserChatMessageService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.socketio.api.ReturnValueSocketResult;
import io.github.loncra.framework.socketio.api.SocketResult;
import io.github.loncra.framework.socketio.api.metadata.AbstractSocketMessageMetadata;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;


/**
 * tb_user_chat_message 的控制器
 *
 * <p>Table: tb_user_chat_message - 聊天房间消息</p>
 *
 * @author maurice.chen
 * @see UserChatMessageEntity
 * @since 2025-06-01 06:31:44
 */
@RestController
@RequestMapping("user/chat/message")
@RequiredArgsConstructor
public class UserChatMessageController {

    private final UserChatMessageService userChatMessageService;

    @GetMapping("/count/readable/{chatRoomId:\\d+}")
    public RestResult<Long> countReadable(
            @PathVariable Long chatRoomId,
            @CurrentSecurityContext SecurityContext securityContext
    ){
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        long count = userChatMessageService.countReadable(chatRoomId, token.getName());
        return RestResult.ofSuccess(count);
    }

    @DeleteMapping("undo")
    @PreAuthorize("isFullyAuthenticated()")
    public SocketResult undo(
            @RequestParam List<String> ids,
            @CurrentSecurityContext SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        List<AbstractSocketMessageMetadata<Object>> messages = userChatMessageService.undo(ids, token);
        return ReturnValueSocketResult.of("操作成功", new LinkedList<>(messages));
    }
}
