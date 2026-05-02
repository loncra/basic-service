package io.github.loncra.basic.service.auth.server.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.github.loncra.basic.service.auth.api.domain.AbstractWechatAuthentication;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.baisc.VersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * <p>Table: tb_wechat_authentication - 第三方认证信息</p>
 *
 * @author maurice.chen
 * @since 2025-05-08 03:39:57
 */
@Data
@NoArgsConstructor
@Alias("wechatAuthentication")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_wechat_authentication", autoResultMap = true)
public class WechatAuthenticationEntity extends AbstractWechatAuthentication implements VersionEntity<Integer, Long> {

    @Serial
    private static final long serialVersionUID = -7494189267518554402L;

    private Long id;

    @Version
    private Integer version = 0;

    private Instant creationTime = Instant.now();

    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> metadata = new LinkedHashMap<>();
}