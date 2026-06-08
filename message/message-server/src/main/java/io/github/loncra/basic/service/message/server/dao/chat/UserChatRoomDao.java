package io.github.loncra.basic.service.message.server.dao.chat;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatRoomEntity;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * tb_user_chat_room 的数据访问
 *
 * <p>Table: tb_user_chat_room - 聊天房间</p>
 *
 * @author maurice.chen
 * @see UserChatRoomEntity
 * @since 2025-06-01 06:31:44
 */
@Mapper
@Repository
public interface UserChatRoomDao extends BaseMapper<UserChatRoomEntity> {

}
