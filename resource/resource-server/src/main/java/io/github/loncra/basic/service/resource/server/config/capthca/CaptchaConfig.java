package io.github.loncra.basic.service.resource.server.config.capthca;

import io.github.loncra.basic.service.resource.api.domain.metadata.DataDictionaryMetadata;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("loncra.basic-service.resource.app.captcha")
public class CaptchaConfig implements Serializable {

    /**
     * 验证码变量名称
     */
    private String codeVariableName = DataDictionaryMetadata.CODE_FIELD;

    /**
     * 验证码超时时间变量名称
     */
    private String expireTimeVariableName = "expireTime";

    /**
     * 验证码操作区域名称
     */
    private String operationVariableName = "operation";
}
