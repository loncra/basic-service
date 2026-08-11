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

    private String clarification = """
            ## Clarify before controlled tools
            These tools are clarify-gated: {0}.
            1. If the request is already clear enough to call a gated tool safely, skip clarify and call that tool directly.
            2. If it is too vague, call `clarify_enter` (with `clarify_target_name` and `clarify_tool_name`), then ask **one** question at a time with `clarify_ask` and show that same question in plain chat text.
            3. Wait for the user's typed reply (normal chat). Repeat step 2 only if still unclear.
            4. When you have enough information, call `clarify_exit`, then call the gated tool. While clarify mode is active (after `clarify_enter`, before `clarify_exit`), do not call gated tools.
            """;

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
            <#if clarification?? && clarification?has_content>
            
            ${clarification}
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
            <#if guidance?? && additionalInformation?has_content>
            
            ${additionalInformation}
            </#if>
            """;

    private Map<String, List<IdValueMetadata<String, String>>> descriptionMap = new LinkedHashMap<>();
}
