package io.github.loncra.basic.service.ai.api.domain.metadata.hub;

import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用户安装 metadata 多态基类，由 {@link PluginTargetTypeEnum} 选择具体类型。
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public abstract class AbstractUserPluginInstallMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 2718463910572049183L;

    private Map<String, Object> metadata = new LinkedHashMap<>();

    abstract public PluginTargetTypeEnum getType();


}
