package io.github.loncra.basic.service.message.server.controller.chat;

import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.message.server.domain.entity.chat.*;
import io.github.loncra.basic.service.message.server.enumerate.chat.UserChatParticipantTypeEnum;
import io.github.loncra.basic.service.message.server.service.chat.UserChatManager;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.HttpRequestParameterMapUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.socketio.api.ReturnValueSocketResult;
import io.github.loncra.framework.socketio.api.SocketResult;
import io.github.loncra.framework.socketio.api.metadata.AbstractSocketMessageMetadata;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
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
        sources = {ResourceSourceEnum.CONSOLE_SOURCE_VALUE, ResourceSourceEnum.PERSONAL_SOURCE_VALUE}
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
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "false")
            boolean withoutReadableAnchor,
            @RequestParam(required = false, defaultValue = "false")
            boolean totalPage,
            @PathVariable
            Long chatRoomId,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        MultiValueMap<String, String> parameter = HttpRequestParameterMapUtils.castMapToMultiValueMap(request.getParameterMap());
        MultiValueMap<String, Object> filter = new LinkedMultiValueMap<>();
        parameter.forEach(filter::addAll);
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return userChatManager.histories(
                pageRequest,
                filter,
                chatRoomId,
                withoutReadableAnchor,
                totalPage,
                token
        );
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
    public UserChatConversationEntity createConversation(
            @Valid
            @RequestBody
            UserChatRoomEntity userChatRoomEntity,
            @RequestParam
            List<String> principals,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return userChatManager.createConversation(userChatRoomEntity, token, principals);
    }

    @PutMapping("/participant/add/{roomId:\\d+}")
    public SocketResult addRoomParticipant(
            @PathVariable
            Long roomId,
            @RequestParam
            List<String> principals,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return userChatManager.addRoomParticipant(roomId, principals, token);
    }

    @PutMapping("/participant/remove/{roomId:\\d+}")
    public SocketResult removeRoomParticipant(
            @PathVariable
            Long roomId,
            @RequestParam
            List<String> principals,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        List<AbstractSocketMessageMetadata<Object>> messages = userChatManager.removeRoomParticipant(roomId, principals, token);
        return ReturnValueSocketResult.of("操作成功", new LinkedList<>(messages));
    }

    @PutMapping("/participant/update/type/{roomId:\\d+}")
    public SocketResult updateRoomParticipantType(
            @PathVariable
            Long roomId,
            @RequestParam
            List<String> principals,
            Integer type,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        UserChatParticipantTypeEnum typeValue = ValueEnum.ofEnum(UserChatParticipantTypeEnum.class, type);
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        List<AbstractSocketMessageMetadata<Object>> messages = userChatManager.updateRoomParticipantType(
                roomId,
                principals,
                typeValue,
                token
        );

        return ReturnValueSocketResult.of("操作成功", new LinkedList<>(messages));
    }

    @DeleteMapping("/participant/exist/room")
    public SocketResult participantExitRoom(
            @RequestParam
            Long roomId,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        List<AbstractSocketMessageMetadata<Object>> messages =  userChatManager.participantExitRoom(roomId, token);

        return ReturnValueSocketResult.of("操作成功", new LinkedList<>(messages));
    }

    @DeleteMapping("/room/disband")
    public SocketResult disbandRoom(
            @RequestParam
            Long roomId,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        List<AbstractSocketMessageMetadata<Object>> messages = userChatManager.disbandRoom(roomId, token);
        return ReturnValueSocketResult.of("操作成功", new LinkedList<>(messages));
    }

    @PostMapping("/participant/find/{roomId:\\d+}")
    public List<UserChatParticipantEntity> findParticipant(
            @PathVariable
            Long roomId,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return userChatManager.findRoomParticipant(roomId, token);
    }

    @PutMapping("/room/rename/{roomId:\\d+}")
    public ReturnValueSocketResult<Void> renameRoom(
            @PathVariable
            Long roomId,
            @RequestParam
            String newName,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        List<AbstractSocketMessageMetadata<Object>> messages = userChatManager.roomRename(roomId, newName, token);
        return ReturnValueSocketResult.of("操作成功", new LinkedList<>(messages));
    }

    @DeleteMapping("message/undo")
    @PreAuthorize("isFullyAuthenticated()")
    public SocketResult undo(
            @RequestParam List<String> ids,
            @CurrentSecurityContext SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        List<AbstractSocketMessageMetadata<Object>> messages = userChatManager.undo(ids, token);
        return ReturnValueSocketResult.of("操作成功", new LinkedList<>(messages));
    }

    @PostMapping("/message/read/find/{messageId:\\d+}")
    public RestResult<List<UserChatMessageReadEntity>> findMessageReader(
            @PathVariable
            Long messageId,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        List<UserChatMessageReadEntity> result = userChatManager.findMessageReader(messageId, token);
        return RestResult.ofSuccess(result);
    }

    @GetMapping("conversation/{chatRoomId:\\d+}")
    @PreAuthorize("isAuthenticated()")
    public UserChatConversationEntity getConversationByRoomId(
            @PathVariable
            Long chatRoomId,
            @RequestParam
            boolean convertBody,
            @CurrentSecurityContext
            SecurityContext  securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return userChatManager.getChatConversationByPrincipal(token.getName(), chatRoomId, convertBody);
    }
}
