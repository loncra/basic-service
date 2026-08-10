package io.github.loncra.basic.service.ai.server.config;

import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 技能配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("loncra.basic-service.ai.app.skill")
public class SkillConfig {

    /**
     * 项目级别的技能文件夹路径
     */
    private String path = "./.skill/";

    private boolean autoReload = true;

    private String filename = "SKILL.md";

    private TimeProperties timeout = new TimeProperties(1, TimeUnit.MINUTES);

    private String contentTemplate = """
            ---
            name: ${id}
            description: Use when the task needs ${group.getName()}<#if tags?? && tags?has_content>, ${tags?join(", ")}</#if>. Then call reset_equipped_tools with to_activate containing "${id}".
            ---
            
            # ${name}
            
            Synced at: ${creationTime}
            Tool group: `${id}`
            
            ## How to use
            1. Call `reset_equipped_tools` with `to_activate: ["${id}"]`
            2. Call the tools below as needed
            3. Deactivate the group when finished
            <#if guidance?? && guidance?has_content>
            
            ${guidance}
            </#if>
            
            ## Available tools
            <#list tools as tool>
            ### `${tool.name()}`
            ${tool.description()!}
            
            Input schema:
            ```json
            ${ObjectMapper.writeValueAsString(tool.inputSchema())}
            ```
            </#list>
            """;

    private Map<String, List<IdValueMetadata<String, String>>> descriptionMap = new LinkedHashMap<>();
}
