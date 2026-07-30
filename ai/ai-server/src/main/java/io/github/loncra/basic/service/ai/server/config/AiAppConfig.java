package io.github.loncra.basic.service.ai.server.config;

import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 全局应用配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@EqualsAndHashCode
@ConfigurationProperties("loncra.basic-service.ai.app")
public class AiAppConfig {

    private Map<String, String> key = new LinkedHashMap<>();

    private String workspacePath = ".agentscope/workspace";

    /** 上下文压缩配置，enabled=false 时整个 compaction 不生效 */
    private Compaction compaction = new Compaction();

    // ═══════════════════════════════════════════════════════
    // 转换方法：yml 配置 → CompactionConfig (agentScope 对象)
    // ═══════════════════════════════════════════════════════

    public CompactionConfig toCompactionConfig() {
        if (compaction == null || !compaction.isEnabled()) {
            return null;
        }

        CompactionConfig.Builder builder = CompactionConfig.builder()
                // ── 触发策略 ──
                .triggerMessages(compaction.getTriggerMessages())   // 默认 50, 0=关闭条数触发
                .triggerTokens(compaction.getTriggerTokens())       // 默认 0=动态模式
                .reserved(compaction.getReserved())                 // 默认 20000, 动态模式预留

                // ── 保留策略 ──
                .keepMessages(compaction.getKeepMessages())         // 默认 20
                .keepTokens(compaction.getKeepTokens())             // 默认 -1=动态模式
                .keepTokensMin(compaction.getKeepTokensMin())       // 默认 2000
                .keepTokensMax(compaction.getKeepTokensMax())       // 默认 8000
                .keepTokensRatio(compaction.getKeepTokensRatio())   // 默认 0.25

                // ── 行为开关 ──
                .flushBeforeCompact(compaction.isFlushBeforeCompact())    // 默认 true
                .offloadBeforeCompact(compaction.isOffloadBeforeCompact());// 默认 true
        ;

        // ── 摘要专用模型（字符串形式通过 ModelRegistry.resolve 解析）──
        if (compaction.getModelId() != null && !compaction.getModelId().isBlank()) {
            builder.model(compaction.getModelId());
        }

        // ── 自定义摘要提示 ──
        if (compaction.getSummaryPrompt() != null && !compaction.getSummaryPrompt().isBlank()) {
            builder.summaryPrompt(compaction.getSummaryPrompt());
        }

        // ── 参数截断子配置 ──
        if (compaction.getTruncateArgs() != null && compaction.getTruncateArgs().isEnabled()) {
            Compaction.TruncateArgs ta = compaction.getTruncateArgs();
            builder.truncateArgs(CompactionConfig.TruncateArgsConfig.builder()
                    .triggerMessages(ta.getTriggerMessages())   // 默认 25
                    .triggerTokens(ta.getTriggerTokens())       // 默认 40000
                    .keepMessages(ta.getKeepMessages())         // 默认 20
                    .keepTokens(ta.getKeepTokens())             // 默认 0
                    .maxArgLength(ta.getMaxArgLength())         // 默认 2000
                    .truncationText(ta.getTruncationText())     // 默认 "...(argument truncated)"
                    .build());
        }

        // ── 工具结果裁剪子配置 ──
        if (compaction.getPrune() != null && compaction.getPrune().isEnabled()) {
            Compaction.Prune p = compaction.getPrune();
            CompactionConfig.PruneConfig.PruneBuilder pruneBuilder =
                    CompactionConfig.PruneConfig.builder()
                            .protectTokens(p.getProtectTokens())     // 默认 40000
                            .minimumTokens(p.getMinimumTokens())     // 默认 20000
                            .maxOutputChars(p.getMaxOutputChars());  // 默认 2000

            if (p.getExcludedTools() != null && !p.getExcludedTools().isEmpty()) {
                pruneBuilder.excludedTools(
                        new LinkedHashSet<>(p.getExcludedTools()));
            }
            builder.prune(pruneBuilder.build());
        } else if (compaction.getPrune() != null && !compaction.getPrune().isEnabled()) {
            // 显式 disabled → 传 null 关闭裁剪
            builder.prune(null);
        }

        return builder.build();
    }

    // ═══════════════════════════════════════════════════════
    // 内嵌配置类（对应 yml 层级，默认值全部与源码 Builder 对齐）
    // ═══════════════════════════════════════════════════════

    @Data
    public static class Compaction {

        /** 总开关。false 时 toCompactionConfig() 返回 null，不注入 compaction */
        private boolean enabled = true;

        // ── 对应 CompactionConfig.Builder 字段，默认值一字不差 ──

        /** 消息条数触发阈值，0=关闭条数触发 */
        private int triggerMessages = 50;

        /** Token 触发阈值，0=动态模式（从模型 contextWindow - reserved 计算） */
        private int triggerTokens = 0;

        /** 动态模式下预留的 token 缓冲（摘要 prompt + 输出），默认 20000 */
        private int reserved = 20_000;

        /** 压缩后保留尾部消息条数（keepTokens=0 时生效） */
        private int keepMessages = 20;

        /**
         * Token 保留预算。 -1=动态模式, 0=用 keepMessages, >0=静态固定值
         */
        private int keepTokens = -1;

        /** 动态 keep 模式下最小 token 数 */
        private int keepTokensMin = 2_000;

        /** 动态 keep 模式下最大 token 数 */
        private int keepTokensMax = 8_000;

        /** 动态 keep 模式下保留比例（0.25 = 25%） */
        private double keepTokensRatio = 0.25;

        /** 压缩前将事实抽取到长期记忆 */
        private boolean flushBeforeCompact = true;

        /** 压缩前将原文写入 .log.jsonl */
        private boolean offloadBeforeCompact = true;

        /**
         * 自定义摘要提示模板，必须含 {messages} 占位符。
         * 为空时使用框架默认 DEFAULT_SUMMARY_PROMPT。
         */
        private String summaryPrompt;

        /**
         * 摘要专用模型 id（通过 ModelRegistry.resolve 解析），
         * 如 "openai:gpt-4.1-mini"。为空时复用 agent 主模型。
         */
        private String modelId;

        /** 参数预截断配置 */
        private TruncateArgs truncateArgs = new TruncateArgs();

        /** 工具结果裁剪配置 */
        private Prune prune = new Prune();

        // ── TruncateArgs ──

        @Data
        public static class TruncateArgs {
            /** 子开关，false 时 truncateArgs 不注入 */
            private boolean enabled = true;

            /** 消息条数触发截断阈值 */
            private int triggerMessages = 25;

            /** Token 触发截断阈值 */
            private int triggerTokens = 40_000;

            /** 截断时保留最近消息数（不受截断影响） */
            private int keepMessages = 20;

            /** 截断时按 token 保留，0=用 keepMessages */
            private int keepTokens = 0;

            /** 单条工具参数最大字符数 */
            private int maxArgLength = 2_000;

            /** 截断占位文本 */
            private String truncationText = "...(argument truncated)";
        }

        // ── Prune ──

        @Data
        public static class Prune {
            /**
             * 子开关。源码默认 PruneConfig.defaults() 是开启的，
             * 这里为了方便配置也默认 true。
             * false 时传 null 给 builder，完全关闭裁剪。
             */
            private boolean enabled = true;

            /** 最近多少 token 的工具输出受保护，永不裁剪 */
            private int protectTokens = 40_000;

            /** 可裁剪总量超过此值才真正执行裁剪 */
            private int minimumTokens = 20_000;

            /** 裁剪后每条工具结果保留的最大字符数（头+尾预览） */
            private int maxOutputChars = 2_000;

            /** 排除裁剪的工具名集合 */
            private Set<String> excludedTools = new LinkedHashSet<>(
                    Set.of("read_file", "memory_search", "memory_get", "session_search"));
        }
    }
}
