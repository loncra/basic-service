package io.github.loncra.basic.service.message.server.domain.entity.chat.call;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.github.loncra.basic.service.message.server.domain.metadata.chat.UserChatParticipantMetadata;
import io.github.loncra.basic.service.message.server.enumerate.chat.call.UserChatCallParticipantStatusEnum;
import io.github.loncra.basic.service.message.server.enumerate.chat.UserChatParticipantTypeEnum;
import io.github.loncra.framework.mybatis.plus.baisc.VersionEntity;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;
import java.util.Map;


/**
 * <p>Table: tb_user_chat_call_participant - 聊天房间参与者</p>
 *
 * @author maurice.chen
 *
 * @since 2026-06-30 08:37:17
 */
@Data
@NoArgsConstructor
@Alias("userChatCallParticipant")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_user_chat_call_participant", autoResultMap = true)
public class UserChatCallParticipantEntity extends UserChatParticipantMetadata implements VersionEntity<Integer, Long> {

    @Serial
    private static final long serialVersionUID = -3080496901538868066L;

    private Long id;

    private Instant creationTime;

    @Version
    private Integer version;

    /**
     * 聊天通话逐渐 id
     */
    private Long userChatCallId;


    /**
     * 状态
     */
    private UserChatCallParticipantStatusEnum status;

    /**
     * 加入时间
     */
    private Instant joinTime;

    /**
     * 离开时间
     */
    private Instant leaveTime;

}