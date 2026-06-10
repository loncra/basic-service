package io.github.loncra.basic.service.message.server.controller.chat;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatParticipantEntity;
import io.github.loncra.basic.service.message.server.service.chat.UserChatParticipantService;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 *
 * tb_user_chat_participant 的控制器
 *
 * <p>Table: tb_user_chat_participant - 聊天房间参与者</p>
 *
 * @see UserChatParticipantEntity
 *
 * @author maurice.chen
 *
 * @since 2025-06-01 06:31:44
 */
@RestController
@RequestMapping("/user/chat/participant")
@RequiredArgsConstructor
public class UserChatParticipantController {

    private final UserChatParticipantService userChatParticipantService;

    /**
     * 获取 table: tb_user_chat_participant 实体集合
     *
     * @param request  http servlet request
     *
     * @return tb_user_chat_participant 实体集合
     *
     * @see UserChatParticipantEntity
    */
    @PostMapping("find")
    @PreAuthorize("isAuthenticated()")
    public List<UserChatParticipantEntity> find(HttpServletRequest request) {
        QueryWrapper<UserChatParticipantEntity> query = userChatParticipantService
            .getQueryGenerator()
            .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return userChatParticipantService.find(query);
    }

    /**
     * 删除 table: tb_user_chat_participant 实体
     *
     * @param ids 主键 ID 值集合
     *
     * @see UserChatParticipantEntity
     */
    @OperationDataTrace
    @PostMapping("delete")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        userChatParticipantService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
