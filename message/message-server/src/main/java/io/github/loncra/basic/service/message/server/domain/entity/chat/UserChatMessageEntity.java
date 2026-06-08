package io.github.loncra.basic.service.message.server.domain.entity.chat;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import io.github.loncra.framework.security.audit.AuditPrincipal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * <p>Table: tb_user_chat_message - 聊天房间消息</p>
 *
 * @author maurice.chen
 *
 * @since 2025-06-01 06:31:44
 */
@Data
@NoArgsConstructor
@Alias("chatMessage")
@TableName(value = "tb_user_chat_message", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class UserChatMessageEntity extends LongVersionEntity<Integer> implements AuditPrincipal {

    @Serial
    private static final long serialVersionUID = 7472157646402407685L;

    /**
     * 业务  id
     */
    private Long chatRoomId;

    /**
     * 内容
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> content;

    /**
     * 发送者
     */
    private String principal;

    /**
     * 是否撤销
     */
    private YesOrNo revoke = YesOrNo.No;
    /**
     * 撤销时间
     */
    private Date revocationTime;

}