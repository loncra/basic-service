package io.github.loncra.basic.service.message.server.domain.entity.chat;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.message.server.enumerate.UserChatRoomBuisnessScenEnum;
import io.github.loncra.basic.service.message.server.enumerate.UserChatRoomTypeEnum;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Map;


/**
 * <p>Table: tb_user_chat_room - 聊天房间</p>
 *
 * @author maurice.chen
 *
 * @since 2025-06-01 06:31:44
 */
@Data
@NoArgsConstructor
@Alias("chatRoom")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_user_chat_room", autoResultMap = true)
public class UserChatRoomEntity extends LongVersionEntity<Integer>  {

    @Serial
    private static final long serialVersionUID = 670440513421342484L;

    /**
     * 业务  id
     */
    private String businessId;

    /**
     * 业务场景
     */
    private UserChatRoomBuisnessScenEnum businessScene;

    /**
     * 房间类型
     */
    private UserChatRoomTypeEnum type;


    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> metadata;
}