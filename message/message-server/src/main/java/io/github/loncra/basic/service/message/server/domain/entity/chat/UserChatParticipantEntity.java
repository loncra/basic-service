package io.github.loncra.basic.service.message.server.domain.entity.chat;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.github.loncra.basic.service.message.server.domain.metadata.chat.UserChatParticipantMetadata;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
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
public class UserChatParticipantEntity extends UserChatParticipantMetadata implements VersionEntity<Integer, Long> {

    @Serial
    private static final long serialVersionUID = -6613713197651424835L;

    private Long id;

    private Instant creationTime;

    /**
     * 是否启用
     */
    // FIXME 记录一个问题，该字段用于在用户退群时，可能房间与用户的关联关系还在，但是该记录已经被删除，导致无法查询到该记录，
    //  所以在退群流程流程时，需要将该值设置为 0，不然前端加载历史聊天记录会找不到用户，就显示不了名字导致前端出错
    private YesOrNo enabled;

    @Version
    private Integer version;

    /**
     * 聊天房间 id
     */
    private Long userChatRoomId;

}