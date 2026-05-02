package io.github.loncra.basic.service.message.api.domian.metadata;

import io.github.loncra.basic.service.commons.enumerate.CloudChannelEnum;
import io.github.loncra.basic.service.message.api.enumerate.MessageTypeEnum;
import io.github.loncra.framework.commons.id.StringIdEntity;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * 短信默认元数据信息
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmsTemplateMetadata extends StringIdEntity {

    @Serial
    private static final long serialVersionUID = 2884649056330189598L;
    /**
     * 渠道名称
     */
    @NotEmpty
    private CloudChannelEnum channel;

    /**
     * 名称
     */
    @NotEmpty
    private String name;

    /**
     * 内容
     */
    @NotEmpty
    private String content;

    /**
     * 类型
     */
    @NotEmpty
    private MessageTypeEnum type = MessageTypeEnum.CAPTCHA;

    /**
     * 额外元数据信息
     */
    private Map<String, Object> metadata = new LinkedHashMap<>();

    private String remark;

}
