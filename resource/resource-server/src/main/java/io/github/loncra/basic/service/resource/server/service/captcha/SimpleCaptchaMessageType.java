package io.github.loncra.basic.service.resource.server.service.captcha;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 简单的消息类型实体实现
 *
 * @author maurice
 */
@Data
public class SimpleCaptchaMessageType implements MessageCaptchaType {

    /**
     * 消息类型
     */
    @NotNull(message = "消息类型不能为空")
    private String messageType;

    /**
     * 消息内容中的 spring el 值
     */
    private Map<String, Object> messageSpringElValue = new LinkedHashMap<>();

    public SimpleCaptchaMessageType() {
    }

}
