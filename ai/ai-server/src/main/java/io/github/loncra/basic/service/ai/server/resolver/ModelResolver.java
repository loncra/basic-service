package io.github.loncra.basic.service.ai.server.resolver;

import io.agentscope.core.model.GenerateOptions;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.framework.commons.CastUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public interface ModelResolver {

    boolean support(ModelSettingMetadata model);

    ModelResolverMetadata resolve(
            ModelSettingMetadata modelSetting,
            Map<String, Object> options
    );

    /**
     * 合并默认值与覆盖值，生成目标 Options 对象。
     *
     * @param defaults    默认值（先放入，会被 overrides 覆盖）
     * @param overrides   覆盖值（用户传入）
     * @param optionsType 目标 Options 类型，如 OllamaOptions.class
     * @param <T>         Options 泛型
     */
    default <T> T buildOptions(
            Map<String, Object> defaults,
            Map<String, Object> overrides,
            Class<T> optionsType
    ) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(defaults)) {
            merged.putAll(defaults);
        }
        if (overrides != null) {
            merged.putAll(overrides);   // overrides 覆盖同 key 的默认值
        }
        return CastUtils.convertValue(merged, optionsType);
    }

    /**
     * 合并默认值和覆盖值，用 Builder 手动构造 GenerateOptions。
     * 不走 CastUtils.convertValue，因为 GenerateOptions 没有无参构造器。
     */
    default GenerateOptions buildGenerateOptions(
            Map<String, Object> defaults,
            Map<String, Object> overrides
    ) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (defaults != null) {
            merged.putAll(defaults);
        }
        if (overrides != null) {
            merged.putAll(overrides);
        }

        GenerateOptions.Builder builder = GenerateOptions.builder();

        // ── 生成参数 ──
        setIfPresent(merged, "temperature", v -> builder.temperature(toDouble(v)));
        setIfPresent(merged, "topP", v -> builder.topP(toDouble(v)));
        setIfPresent(merged, "topK", v -> builder.topK(toInt(v)));
        setIfPresent(merged, "maxTokens", v -> builder.maxTokens(toInt(v)));
        setIfPresent(merged, "maxCompletionTokens", v -> builder.maxCompletionTokens(toInt(v)));
        setIfPresent(merged, "frequencyPenalty", v -> builder.frequencyPenalty(toDouble(v)));
        setIfPresent(merged, "presencePenalty", v -> builder.presencePenalty(toDouble(v)));
        setIfPresent(merged, "seed", v -> builder.seed(toLong(v)));
        setIfPresent(merged, "thinkingBudget", v -> builder.thinkingBudget(toInt(v)));
        setIfPresent(merged, "reasoningEffort", v -> builder.reasoningEffort(String.valueOf(v)));
        setIfPresent(merged, "cacheControl", v -> builder.cacheControl(toBoolean(v)));
        setIfPresent(merged, "parallelToolCalls", v -> builder.parallelToolCalls(toBoolean(v)));

        // ── 连接级配置 ──
        setIfPresent(merged, "stream", v -> builder.stream(toBoolean(v)));

        return builder.build();
    }

    // ── 安全类型转换 ──

    private Integer toInt(Object v) {
        if (v instanceof Number n) return n.intValue();
        return Integer.parseInt(String.valueOf(v));
    }

    private Long toLong(Object v) {
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(String.valueOf(v));
    }

    private Double toDouble(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        return Double.parseDouble(String.valueOf(v));
    }

    private Boolean toBoolean(Object v) {
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(v));
    }

    private void setIfPresent(
            Map<String, Object> map,
            String key,
            java.util.function.Consumer<Object> setter
    ) {
        Object value = map.get(key);
        if (value != null) {
            setter.accept(value);
        }
    }
}
