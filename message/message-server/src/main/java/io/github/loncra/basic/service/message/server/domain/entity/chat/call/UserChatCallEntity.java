package io.github.loncra.basic.service.message.server.domain.entity.chat.call;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.message.server.enumerate.chat.call.UserChatCallSceneEnum;
import io.github.loncra.basic.service.message.server.enumerate.chat.call.UserChatCallStatusEnum;
import io.github.loncra.basic.service.message.server.enumerate.chat.call.UserChatCallTypeEnum;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * <p>Table: tb_user_chat_call - 聊天通话表</p>
 *
 * @author maurice.chen
 *
 * @since 2026-06-30 08:37:17
 */
@Data
@NoArgsConstructor
@Alias("userChatCall")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_user_chat_call", autoResultMap = true)
public class UserChatCallEntity extends LongVersionEntity<Integer> {

    public static final String ROOM_ID_PREFIX = "user_chat_call";

    @Serial
    private static final long serialVersionUID = -3340328109918301095L;

    /**
     * 业务  id
     */
    private Long userChatRoomId;

    /**
     * 房间类型
     */
    private UserChatCallTypeEnum type;

    /**
     * 元数据信息
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> metadata = new LinkedHashMap<>();

    /**
     * 开始时间
     */
    private Instant startTime;

    /**
     * 结束时间
     */
    private Instant endTime;

    /**
     * 状态
     */
    private UserChatCallStatusEnum status;

    /**
     * 场景
     */
    private UserChatCallSceneEnum scene;

    /**
     * 名称
     */
    private String name;

    public String getRoomId() {
        return ROOM_ID_PREFIX + CacheProperties.DEFAULT_SEPARATOR + getId();
    }

}