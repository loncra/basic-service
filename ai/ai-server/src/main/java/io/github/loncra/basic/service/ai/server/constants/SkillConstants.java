package io.github.loncra.basic.service.ai.server.constants;

/**
 * MCP 伴生 Skill 同步相关常量（{@code AiMcpPackageEntity.metadata}）
 *
 * @author maurice.chen
 */
public final class SkillConstants {

    private SkillConstants() {
    }

    /** metadata 根键：伴生 Skill 扩展 */
    public static final String METADATA_SKILL_KEY = "skill";

    /** metadata.skill.guidance：写入 SKILL.md 正文的额外 Markdown 段落 */
    public static final String SKILL_GUIDANCE = "guidance";
}
