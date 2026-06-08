package io.github.loncra.basic.service.message.server.service.chat;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.message.server.dao.chat.UserChatRoomDao;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatRoomEntity;
import io.github.loncra.basic.service.message.server.enumerate.UserChatRoomBuisnessScenEnum;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * tb_user_chat_room 的业务逻辑
 *
 * <p>Table: tb_user_chat_room - 聊天房间</p>
 *
 * @see UserChatRoomEntity
 *
 * @author maurice.chen
 *
 * @since 2025-06-01 06:31:44
 */
@Service
@RequiredArgsConstructor
public class UserChatRoomService extends BasicService<UserChatRoomDao, UserChatRoomEntity> {

    public UserChatRoomEntity getByBusiness(
            String businessId,
            UserChatRoomBuisnessScenEnum businessScene
    ) {
        return lambdaQuery().eq(UserChatRoomEntity::getBusinessId, businessId)
                .eq(UserChatRoomEntity::getBusinessScene, businessScene)
                .one();
    }
}
