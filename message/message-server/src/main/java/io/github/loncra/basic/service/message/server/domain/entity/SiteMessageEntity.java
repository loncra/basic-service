package io.github.loncra.basic.service.message.server.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.commons.domain.AttachmentMessage;
import io.github.loncra.framework.commons.annotation.JsonCollectionGenericType;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>站内信消息实体类</p>
 * <p>Table: tb_site_message - 站内信消息</p>
 *
 * @author maurice
 * @since 2020-05-06 03:48:46
 */
@Data
@NoArgsConstructor
@Alias("siteMessage")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_site_message", autoResultMap = true)
public class SiteMessageEntity extends BasicMessageEntity implements AttachmentMessage, BatchMessageEntity.Body {


    @Serial
    private static final long serialVersionUID = 2037280001998945900L;

    public static final String TO_USER_TABLE_FIELD = "to_user";

    /**
     * 标题
     */
    private String title;
    /**
     * 渠道商
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<String> channel;

    /**
     * 收信用户
     */
    private String toUser;

    /**
     * 操作人
     * @deprecated 通过系统审计去记录发送者就好
     */
    @Deprecated
    private String principal;

    /**
     * 是否可推送的消息：0.否，1.是
     */
    private YesOrNo pushable = YesOrNo.Yes;

    /**
     * 是否可读的：0.否，1.是
     */
    private YesOrNo readable = YesOrNo.Yes;

    /**
     * 读取时间
     */
    private Instant readTime;

    /**
     * 数据
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> metadata;

    /**
     * 批量消息 id
     */
    private Long batchId;

    /**
     * 附件集合
     */
    @JsonCollectionGenericType(ObjectWriteResult.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<ObjectWriteResult> attachmentList = new ArrayList<>();

    /**
     * 封面
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private ObjectWriteResult cover;

}