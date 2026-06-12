package io.github.loncra.basic.service.message.server.controller.chat;

import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatConversationEntity;
import io.github.loncra.basic.service.message.server.service.chat.UserChatConversationService;
import io.github.loncra.framework.commons.RestResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * tb_user_chat_conversation 的控制器
 *
 * @see UserChatConversationEntity
 *
 * @author maurice.chen
 *
 * @since 2026-06-05 10:30:49
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("user/chat/conversation")
public class UserChatConversationController {

    private final UserChatConversationService userChatConversationService;

    /**
     * 删除数据
     *
     * @param ids 主键 ID 值集合
     *
     * @see UserChatConversationEntity
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public RestResult<Void> delete(@RequestParam List<Long> ids) {
        userChatConversationService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    @PutMapping("pinned")
    @PreAuthorize("isAuthenticated()")
    public RestResult<List<UserChatConversationEntity>> pinned(@RequestParam List<Long> ids) {
        return RestResult.ofSuccess(userChatConversationService.pinned(ids));
    }

    @PutMapping("muted")
    @PreAuthorize("isAuthenticated()")
    public RestResult<List<UserChatConversationEntity>> muted(@RequestParam List<Long> ids) {
        return RestResult.ofSuccess(userChatConversationService.muted(ids));
    }
}
