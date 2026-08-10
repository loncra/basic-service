package io.github.loncra.basic.service.ai.server.service.clarify;

import io.github.loncra.basic.service.ai.server.constants.ClarifyConstants;
import io.github.loncra.basic.service.ai.server.domain.clarify.ClarifySessionState;
import io.github.loncra.basic.service.ai.server.domain.clarify.ClarifyToolPolicy;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clarify Mode 会话状态（按 conversation/sessionId 隔离，并镜像到助手消息 metadata）
 */
@Component
@RequiredArgsConstructor
public class ClarifyModeManager {

    private final Map<String, ClarifySessionState> sessions = new ConcurrentHashMap<>();

    private final ClarifyPolicyResolver policyResolver;

    public ClarifySessionState getOrCreate(String sessionId) {
        return sessions.computeIfAbsent(sessionId, id -> new ClarifySessionState());
    }

    public Optional<ClarifySessionState> find(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public void hydrateFromMessage(String sessionId, AgentMessageEntity assistant) {
        if (Objects.isNull(assistant) || MapUtils.isEmpty(assistant.getMetadata())) {
            return;
        }
        Object raw = assistant.getMetadata().get(ClarifyConstants.MESSAGE_CLARIFY_SESSION_KEY);
        if (Objects.isNull(raw)) {
            return;
        }
        ClarifySessionState state = CastUtils.convertValue(raw, ClarifySessionState.class);
        if (Objects.nonNull(state)) {
            sessions.put(sessionId, state);
        }
    }

    public void persistToMessage(String sessionId, AgentMessageEntity assistant) {
        ClarifySessionState state = getOrCreate(sessionId);
        assistant.getMetadata().put(ClarifyConstants.MESSAGE_CLARIFY_SESSION_KEY, state);
        assistant.refreshMetadataJson();
    }

    public void enter(String sessionId, String targetTool) {
        ClarifySessionState state = getOrCreate(sessionId);
        state.setActive(true);
        if (StringUtils.isNotBlank(targetTool)) {
            state.setTargetTool(targetTool);
        }
        state.setWriteDone(false);
    }

    public void writeCard(String sessionId, String targetTool, Map<String, Object> card) {
        ClarifySessionState state = getOrCreate(sessionId);
        state.setActive(true);
        if (StringUtils.isNotBlank(targetTool)) {
            state.setTargetTool(targetTool);
        }
        state.setCard(card == null ? new LinkedHashMap<>() : new LinkedHashMap<>(card));
        state.setWriteDone(true);
        state.setRounds(state.getRounds() + 1);
        state.setDimensionsSatisfied(false);
    }

    /**
     * exit 执行阶段校验：只要求已 write（用户提交后 active 可能已为 false，不得再要求 active）。
     */
    public void markExitPending(String sessionId) {
        ClarifySessionState state = getOrCreate(sessionId);
        SystemException.isTrue(state.isWriteDone(), ClarifyConstants.ERR_EXIT_NEED_WRITE);
        Optional<ClarifyToolPolicy> policy = policyResolver.findToolPolicy(state.getTargetTool());
        policy.ifPresent(p -> SystemException.isTrue(
                state.getRounds() <= p.getMaxClarifyRounds(),
                "澄清轮次已达上限 [" + p.getMaxClarifyRounds() + "]"
        ));
    }

    public void applyAnswers(String sessionId, Map<String, Object> answers) {
        ClarifySessionState state = getOrCreate(sessionId);
        state.setAnswers(answers == null ? new LinkedHashMap<>() : new LinkedHashMap<>(answers));
        state.setDimensionsSatisfied(true);
        // 保持 writeDone=true，供 resume 后 clarify_exit.callAsync → markExitPending
        state.setActive(false);
    }

    public void cancel(String sessionId) {
        ClarifySessionState state = getOrCreate(sessionId);
        state.setActive(false);
        state.setDimensionsSatisfied(false);
        state.setAnswers(new LinkedHashMap<>());
        state.setWriteDone(false);
        state.setCard(new LinkedHashMap<>());
    }

    public boolean isWriteDone(String sessionId) {
        return find(sessionId).map(ClarifySessionState::isWriteDone).orElse(false);
    }

    public boolean isActive(String sessionId) {
        return find(sessionId).map(ClarifySessionState::isActive).orElse(false);
    }

    public boolean canExecuteControlledTool(String sessionId, String toolName) {
        Optional<ClarifyToolPolicy> policyOpt = policyResolver.findToolPolicy(toolName);
        if (policyOpt.isEmpty()) {
            return true;
        }
        ClarifyToolPolicy policy = policyOpt.get();
        ClarifySessionState state = getOrCreate(sessionId);
        if (state.isActive()) {
            return false;
        }
        if (!state.isDimensionsSatisfied()) {
            return false;
        }
        int used = state.getCallCounts().getOrDefault(toolName, 0);
        return used < policy.getMaxCallsPerTurn();
    }

    public void incrementCallCount(String sessionId, String toolName) {
        ClarifySessionState state = getOrCreate(sessionId);
        state.getCallCounts().merge(toolName, 1, Integer::sum);
    }

    public boolean isBudgetExceeded(String sessionId, String toolName) {
        Optional<ClarifyToolPolicy> policyOpt = policyResolver.findToolPolicy(toolName);
        if (policyOpt.isEmpty()) {
            return false;
        }
        ClarifySessionState state = getOrCreate(sessionId);
        int used = state.getCallCounts().getOrDefault(toolName, 0);
        return used >= policyOpt.get().getMaxCallsPerTurn();
    }

    public void validateAnswers(String toolName, Map<String, Object> answers) {
        ClarifyToolPolicy policy = policyResolver.findToolPolicy(toolName)
                .orElseThrow(() -> new SystemException("找不到工具 [" + toolName + "] 的澄清策略"));
        SystemException.isTrue(MapUtils.isNotEmpty(answers), "澄清 answers 不能为空");
        for (String dim : policy.getRequiredDimensions()) {
            Object value = answers.get(dim);
            SystemException.isTrue(
                    Objects.nonNull(value) && StringUtils.isNotBlank(String.valueOf(value)),
                    "缺少必填澄清维度 [" + dim + "]"
            );
        }
    }

    public void validateCardAgainstPolicy(String toolName, Map<String, Object> card) {
        ClarifyToolPolicy policy = policyResolver.findToolPolicy(toolName)
                .orElseThrow(() -> new SystemException("找不到工具 [" + toolName + "] 的澄清策略"));
        SystemException.isTrue(MapUtils.isNotEmpty(card), "clarify_write content 不能为空");
        String cardJson = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(card));
        if (StringUtils.containsIgnoreCase(cardJson, ClarifyConstants.WIDGET_TEXTAREA)) {
            throw new SystemException("澄清表单禁止使用 textarea；请使用 TextField/SelectField/CheckboxField，开放描述引导用户在输入框重述");
        }
        List<Map<String, Object>> commands = CastUtils.convertValue(
                card.get(ClarifyConstants.CARD_COMMANDS),
                new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
                }
        );
        SystemException.isTrue(CollectionUtils.isNotEmpty(commands), "clarify_write content.commands 不能为空（A2UI v0.9）");
        for (String dim : policy.getRequiredDimensions()) {
            boolean hit = StringUtils.contains(cardJson, "\"/" + dim + "\"")
                    || StringUtils.contains(cardJson, "\"bindPath\":\"" + dim + "\"")
                    || StringUtils.contains(cardJson, "\"bindPath\": \"" + dim + "\"")
                    || StringUtils.contains(cardJson, "\"path\":\"/" + dim + "\"")
                    || StringUtils.contains(cardJson, "\"path\": \"/" + dim + "\"");
            SystemException.isTrue(hit, "澄清 A2UI commands 缺少必填维度路径 [/" + dim + "] 或 bindPath [" + dim + "]");
        }
    }

    public void saveClarifyResultsMetadata(AgentMessageEntity assistant, Map<String, Object> clarifyResult) {
        List<Map<String, Object>> list = CastUtils.convertValue(
                assistant.getMetadata().get(ClarifyConstants.METADATA_CLARIFY_RESULTS),
                new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
                }
        );
        if (list == null) {
            list = new LinkedList<>();
        }
        list.add(clarifyResult);
        assistant.getMetadata().put(ClarifyConstants.METADATA_CLARIFY_RESULTS, list);
        assistant.refreshMetadataJson();
    }

    public List<Map<String, Object>> obtainClarifyResultsThenRemove(AgentMessageEntity assistant) {
        List<Map<String, Object>> list = CastUtils.convertValue(
                assistant.getMetadata().get(ClarifyConstants.METADATA_CLARIFY_RESULTS),
                new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
                }
        );
        assistant.getMetadata().remove(ClarifyConstants.METADATA_CLARIFY_RESULTS);
        assistant.refreshMetadataJson();
        return list == null ? Collections.emptyList() : list;
    }
}
