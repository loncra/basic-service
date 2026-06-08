package io.github.loncra.basic.service.message.server.controller.chat;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatConversationEntity;
import io.github.loncra.basic.service.message.server.service.chat.UserChatConversationService;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.plugin.Plugin;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
     * 获取分页
     *
     * @param pageRequest 分页信息
     * @param request  http servlet request
     *
     * @return 分页实体
     *
     * @see UserChatConversationEntity
     */
    @PostMapping
    @PreAuthorize("hasAuthority('perms[user_chat_conversation:page]')")
    public Page<UserChatConversationEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<UserChatConversationEntity> query = userChatConversationService
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return userChatConversationService.findPage(pageRequest, query);
    }

    /**
     * 获取明细
     *
     * @param id 主键 ID
     *
     * @return REST 响应结果
     *
     * @see UserChatConversationEntity
     */
    @GetMapping("/{id:\\d+}")
    @PreAuthorize("isAuthenticated()")
    public UserChatConversationEntity get(@RequestParam Integer id) {
        return userChatConversationService.get(id);
    }

    /**
     * 保存数据
     *
     * @param entity 数据请求体
     *
     * @see UserChatConversationEntity
     */
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public RestResult<Long> save(@Valid @RequestBody UserChatConversationEntity entity) {
        userChatConversationService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除数据
     *
     * @param ids 主键 ID 值集合
     *
     * @see UserChatConversationEntity
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public RestResult<Void> delete(@RequestParam List<Integer> ids) {
        userChatConversationService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
