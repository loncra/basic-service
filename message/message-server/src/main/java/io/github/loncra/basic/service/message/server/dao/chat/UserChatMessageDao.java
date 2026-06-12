package io.github.loncra.basic.service.message.server.dao.chat;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * tb_user_chat_message 的数据访问
 *
 * <p>Table: tb_user_chat_message - 聊天房间消息</p>
 *
 * @see UserChatMessageEntity
 *
 * @author maurice.chen
 *
 * @since 2025-06-01 06:31:44
 */
@Mapper
@Repository
public interface UserChatMessageDao extends BaseMapper<UserChatMessageEntity> {


    @Select("""
        <script>
            SELECT 
              COUNT( chat_message.id ) 
            FROM 
              tb_user_chat_message chat_message 
            LEFT JOIN 
              tb_user_chat_room chat_room 
            ON 
              chat_room.id = chat_message.chat_room_id 
            LEFT JOIN 
              tb_user_chat_message_read chat_message_read 
            ON 
              chat_message_read.chat_message_id = chat_message.id 
            WHERE 
              chat_message_read.principal = #{principal} 
              AND chat_message_read.readable = 1
              AND chat_room.id = #{roomId}
        </script>
    """)
    long countReadable(@Param("roomId") Long roomId, @Param("principal") String principal);

    @Select("""
        <script>
            SELECT 
              chat_message.id
            FROM 
              tb_user_chat_message chat_message 
            LEFT JOIN 
              tb_user_chat_room chat_room 
            ON 
              chat_room.id = chat_message.chat_room_id 
            LEFT JOIN 
              tb_user_chat_message_read chat_message_read 
            ON 
              chat_message_read.chat_message_id = chat_message.id 
            WHERE 
              chat_message_read.principal = #{principal} 
              AND chat_message_read.readable = 1
              AND chat_room.id = #{roomId}
        </script>
    """)
    List<Long> findReadable(@Param("roomId") Long roomId, @Param("principal") String principal);
}
