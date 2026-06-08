package io.github.loncra.basic.service.message.server.domain.entity.chat;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import lombok.*;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;


/**
 * <p>Table: tb_user_chat_message_read - 聊天消息已读列表</p>
 *
 * @author maurice.chen
 *
 * @since 2025-06-01 06:31:44
 */
@Data
@NoArgsConstructor
@Alias("chatMessageRead")
@TableName("tb_user_chat_message_read")
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor(staticName = "of")
public class UserChatMessageReadEntity extends LongVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = -3628835145316774244L;

    /**
     * 业务  id
     */
    @NonNull
    private Long chatMessageId;

    /**
     * 发送者
     */
    @NonNull
    private String principal;

    /**
     * 是否可读
     */
    private YesOrNo readable = YesOrNo.Yes;

    /**
     * 读取时间
     */
    private Instant readTime;

}