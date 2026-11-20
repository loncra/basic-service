package io.github.loncra.basic.service.ai.api.enumerate.hub;

import io.github.loncra.basic.service.ai.api.domain.metadata.hub.AbstractUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.type.McpUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.type.SkillUserPluginInstallMetadata;
import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PluginTargetTypeEnum implements NameValueEnum<String> {

    SKILL("技能", "skill", SkillUserPluginInstallMetadata.class),

    MCP("MCP", "mcp", McpUserPluginInstallMetadata.class),

    ;

    private final String name;

    private final String value;

    private final Class<? extends AbstractUserPluginInstallMetadata> targetClass;
}
