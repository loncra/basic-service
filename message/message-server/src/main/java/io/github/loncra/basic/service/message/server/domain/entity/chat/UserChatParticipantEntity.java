package io.github.loncra.basic.service.message.server.domain.entity.chat;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.github.loncra.basic.service.message.server.domain.metadata.chat.UserChatParticipantMetadata;
import io.github.loncra.framework.mybatis.plus.baisc.VersionEntity;
import io.github.loncra.framework.security.audit.AuditPrincipal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;


/**
 * <p>Table: tb_user_chat_participant - 聊天房间参与者</p>
 *
 * @author maurice.chen
 *
 * @since 2025-06-01 06:31:44
 */
@Data
@NoArgsConstructor
@Alias("chatParticipant")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_user_chat_participant", autoResultMap = true)
public class UserChatParticipantEntity extends UserChatParticipantMetadata implements AuditPrincipal, VersionEntity<Integer, Long> {

    @Serial
    private static final long serialVersionUID = -6613713197651424835L;

    private Long id;

    private Instant creationTime;

    @Version
    private Integer version;

    /**
     * 聊天房间 id
     */
    private Long userChatRoomId;

    /**
     * 参与者
     */
    private String principal;

}