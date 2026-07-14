package io.github.loncra.basic.service.message.server.dao.chat.call;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_user_chat_call 的数据访问
 *
 * <p>Table: tb_user_chat_call - 聊天通话表</p>
 *
 * @see UserChatCallEntity
 *
 * @author maurice.chen
 *
 * @since 2026-06-30 08:37:17
 */
@Mapper
@Repository
public interface UserChatCallDao extends BaseMapper<UserChatCallEntity> {

}
