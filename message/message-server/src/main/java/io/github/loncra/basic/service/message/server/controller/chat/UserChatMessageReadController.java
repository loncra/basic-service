package io.github.loncra.basic.service.message.server.controller.chat;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatMessageReadEntity;
import io.github.loncra.basic.service.message.server.service.chat.UserChatMessageReadService;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.security.plugin.Plugin;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 *
 * tb_user_chat_message_read 的控制器
 *
 * <p>Table: tb_user_chat_message_read - 聊天消息已读列表</p>
 *
 * @see UserChatMessageReadEntity
 *
 * @author maurice.chen
 *
 * @since 2025-06-01 06:31:44
 */
@RestController
@RequestMapping("user/chat/message/read")
@RequiredArgsConstructor
public class UserChatMessageReadController {

    private final UserChatMessageReadService userChatMessageReadService;

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("isAuthenticated()")
    public List<UserChatMessageReadEntity> getByChatMessageId(@PathVariable Long id) {
        return userChatMessageReadService.getByChatMessageId(id);
    }

}
