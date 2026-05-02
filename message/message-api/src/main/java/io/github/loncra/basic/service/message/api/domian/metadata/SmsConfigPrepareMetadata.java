package io.github.loncra.basic.service.message.api.domian.metadata;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信预处理元数据细腻
 *
 * @author maurice.chen
 */
@Data
public class SmsConfigPrepareMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 3720622137180474753L;

    /**
     * 发送短信验证码类型
     */
    private String sendCaptchaType = "tianai";
}
