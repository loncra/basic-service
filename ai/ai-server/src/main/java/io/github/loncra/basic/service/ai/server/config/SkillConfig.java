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
            These tools are blocked until clarify finishes: {0}.
            Hard rule: never call a gated tool before `clarify_exit`. Calling them while status is READY/PENDING will fail.
            Required flow whenever you need a gated tool:
            1. Call `clarify_enter` first with `clarify_mcp_name` (MCP group id) and `clarify_tool_name` (MCP tool name).
            2. If any required argument is still missing, ask **exactly ONE** question in plain chat text, then STOP and wait for the user's reply.
               - Prefer a short multiple-choice question with options labeled A / B / C (and more if needed).
               - Do NOT ask several questions in one message. Do NOT dump a checklist.
            3. After the user replies, ask another single question only if still unclear.
            4. When you have enough information (or the request was already complete), call `clarify_exit`, then call the gated tool.
            If the request is already clear: still call `clarify_enter`, then `clarify_exit` immediately (no question), then the gated tool.
            Never answer market/quote/data from memory — use the gated tool only after `clarify_exit`.
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
            <#if additionalInformation?? && additionalInformation?has_content>
            
            ${additionalInformation}
            </#if>
            """;

    private Map<String, List<IdValueMetadata<String, String>>> descriptionMap = new LinkedHashMap<>();
}
