package io.github.loncra.basic.service.message.api.domian.metadata;

import io.github.loncra.basic.service.commons.enumerate.CloudChannelEnum;
import io.github.loncra.framework.commons.id.StringIdEntity;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 短信签名元数据信息
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmsSignMetadata extends StringIdEntity {

    @Serial
    private static final long serialVersionUID = -2528716390318014593L;

    /**
     * 渠道名称
     */
    @NotEmpty
    private CloudChannelEnum channel;

    /**
     * 签名名称
     */
    @NotEmpty
    private String name;

    /**
     * 额外元数据信息
     */
    private Map<String, Object> metadata = new LinkedHashMap<>();

    /**
     * 备注
     */
    private String remark;

}
