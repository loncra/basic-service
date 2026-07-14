package io.github.loncra.basic.service.message.server.domain.entity.chat;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.message.server.domain.metadata.chat.MessageContentMentionMetadata;
import io.github.loncra.basic.service.message.server.enumerate.chat.UserChatConversationStatusEnum;
import io.github.loncra.framework.commons.annotation.JsonCollectionGenericType;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.minio.FileObject;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;


/**
 * <p>Table: tb_user_chat_conversation - 用户聊天会话记录</p>
 *
 * @author maurice.chen
 *
 * @since 2026-06-05 10:30:49
 */
@Data
@NoArgsConstructor
@Alias("userChatConversation")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_user_chat_conversation", autoResultMap = true)
public class UserChatConversationEntity extends LongVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = -2739967825296343790L;
    /**
     * 所属用户
     */
    private String principal;

    /**
     * 聊天房间 id
     */
    private Long userChatRoomId;

    /**
     * 是否置顶
     */
    private YesOrNo pinned;

    /**
     * 是否免打扰
     */
    private YesOrNo muted;

    /**
     * 最后一条消息内容
     */
    private Long lastUserChatMessageId;

    /**
     * 会话名称
     */
    private String name;

    /**
     * 状态
     */
    private UserChatConversationStatusEnum status;

    /**
     * 提及内容
     */
    @JsonCollectionGenericType(MessageContentMentionMetadata.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<MessageContentMentionMetadata> mentions = new LinkedList<>();

    @JsonCollectionGenericType(FileObject.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<FileObject> cover;

}