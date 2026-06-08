package io.github.loncra.basic.service.message.server.dao.chat;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatConversationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_user_chat_conversation 的数据访问
 *
 * <p>Table: tb_user_chat_conversation - 用户聊天会话记录</p>
 *
 * @see UserChatConversationEntity
 *
 * @author maurice.chen
 *
 * @since 2026-06-05 10:30:49
 */
@Mapper
@Repository
public interface UserChatConversationDao extends BaseMapper<UserChatConversationEntity> {

}
