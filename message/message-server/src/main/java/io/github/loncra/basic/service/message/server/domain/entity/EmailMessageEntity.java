package io.github.loncra.basic.service.message.server.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.commons.domain.AttachmentMessage;
import io.github.loncra.framework.commons.annotation.JsonCollectionGenericType;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.CryptoProperties;
import io.github.loncra.framework.mybatis.plus.annotation.Decryption;
import io.github.loncra.framework.mybatis.plus.annotation.Encryption;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>邮件消息实体类</p>
 * <p>Table: tb_email_message - 邮件消息</p>
 *
 * @author maurice
 * @since 2020-05-06 11:59:41
 */
@Data
@NoArgsConstructor
@Alias("emailMessage")
@TableName(value = "tb_email_message", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class EmailMessageEntity extends BasicMessageEntity implements AttachmentMessage, BatchMessageEntity.Body {

    @Serial
    private static final long serialVersionUID = 8360029094205090328L;

    /**
     * 标题
     */
    @NotNull
    private String title;

    /**
     * 发送邮件
     */
    @NotNull
    private String fromEmail;

    /**
     * 收取邮件
     */
    @NotNull
    @Decryption(beanName = CryptoProperties.MYBATIS_PLUS_DATA_AES_CRYPTO_SERVICE_NAME)
    @Encryption(beanName = CryptoProperties.MYBATIS_PLUS_DATA_AES_CRYPTO_SERVICE_NAME)
    private String toEmail;

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
     * 操作人
     */
    private String principal;

    /**
     * 元数据信息
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> metadata = new LinkedHashMap<>();
}