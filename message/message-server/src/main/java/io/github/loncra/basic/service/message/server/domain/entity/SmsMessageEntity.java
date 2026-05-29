package io.github.loncra.basic.service.message.server.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.enumerate.CloudChannelEnum;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.CryptoProperties;
import io.github.loncra.framework.mybatis.plus.annotation.Decryption;
import io.github.loncra.framework.mybatis.plus.annotation.Encryption;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>短信消息实体类</p>
 * <p>Table: tb_sms_message - 短信消息</p>
 *
 * @author maurice
 * @since 2020-05-06 11:59:41
 */
@Data
@NoArgsConstructor
@Alias("smsMessage")
@TableName(value = "tb_sms_message", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class SmsMessageEntity extends BasicMessageEntity implements BatchMessageEntity.Body {

    @Serial
    private static final long serialVersionUID = 3229810529789017287L;

    /**
     * 渠道名称
     */
    private CloudChannelEnum channel;

    /**
     * 手机号码
     */
    @NotNull
    @Pattern(regexp = SystemConstants.PHONE_NUMBER_REGULAR_EXPRESSION)
    @Decryption(beanName = CryptoProperties.MYBATIS_PLUS_DATA_AES_CRYPTO_SERVICE_NAME)
    @Encryption(beanName = CryptoProperties.MYBATIS_PLUS_DATA_AES_CRYPTO_SERVICE_NAME)
    private String phoneNumber;

    /**
     * 批量消息 id
     */
    private Long batchId;

    /**
     * 元数据信息
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> metadata = new LinkedHashMap<>();

}