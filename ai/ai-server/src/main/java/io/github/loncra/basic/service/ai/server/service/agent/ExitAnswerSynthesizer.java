package io.github.loncra.basic.service.ai.server.service.agent;

import io.agentscope.core.message.ToolCallState;
import io.github.loncra.basic.service.ai.api.enumerate.AgentBlockStatusEnum;
import io.github.loncra.basic.service.ai.server.constants.AgentAnswerConstants;
import io.github.loncra.basic.service.ai.server.constants.ClarifyConstants;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AnswerBlockContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ToolCallBlockContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentSseStreamPublishResolver;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * HITL exit 停住时后端合成 answer 块（markdown / a2ui）。
 */
@Component
@RequiredArgsConstructor
public class ExitAnswerSynthesizer {

    private final AgentSseStreamPublishResolver agentSseStreamPublishResolver;

    public Optional<AnswerBlockContentMetadata> synthesizeAndPublish(AgentMessageEntity assistant) {
        Optional<ToolCallBlockContentMetadata> exitOpt = findPendingExit(assistant);
        if (exitOpt.isEmpty()) {
            return Optional.empty();
        }
        ToolCallBlockContentMetadata exit = exitOpt.get();
        if (hasSynthesizedAnswer(assistant, exit.getId())) {
            return Optional.empty();
        }

        AnswerBlockContentMetadata answer = new AnswerBlockContentMetadata();
        answer.setId("exit-answer-" + exit.getId());
        answer.setAssistantMessageId(assistant.getId());
        answer.setSseEventId(UUID.randomUUID().toString());
        answer.setStatus(AgentBlockStatusEnum.DONE);
        answer.setCreationTime(Instant.now());
        answer.setEndTime(Instant.now());
        answer.setSourceExit(exit.getName());
        answer.setHitlToolCallId(exit.getId());

        Map<String, Object> exitInput = parseToolInput(exit);
        String summary = Objects.toString(exitInput.get(ClarifyConstants.TOOL_PARAM_SUMMARY), null);
        if (StringUtils.isBlank(summary)) {
            summary = Objects.toString(exitInput.get("rationale"), null);
        }

        if (ClarifyConstants.CLARIFY_EXIT.equals(exit.getName())) {
            answer.setFormat(AgentAnswerConstants.FORMAT_A2UI);
            answer.setValue(StringUtils.defaultIfBlank(summary, "请确认以下澄清项后继续。"));
            Optional<ToolCallBlockContentMetadata> write = findPairedWrite(assistant, exit, ClarifyConstants.CLARIFY_WRITE);
            write.ifPresent(w -> fillA2uiFromWrite(answer, w));
        } else if (AgentAnswerConstants.PLAN_EXIT.equals(exit.getName())) {
            answer.setFormat(AgentAnswerConstants.FORMAT_MARKDOWN);
            String planText = StringUtils.defaultIfBlank(summary, "计划已就绪，请确认是否执行。");
            Optional<ToolCallBlockContentMetadata> write = findPairedWrite(assistant, exit, AgentAnswerConstants.PLAN_WRITE);
            if (write.isPresent()) {
                Map<String, Object> writeInput = parseToolInput(write.get());
                Object content = writeInput.get(ClarifyConstants.TOOL_PARAM_CONTENT);
                if (content != null && StringUtils.isNotBlank(String.valueOf(content))) {
                    planText = String.valueOf(content);
                    if (StringUtils.isNotBlank(summary)) {
                        planText = summary + "\n\n" + planText;
                    }
                }
            }
            answer.setValue(planText);
        } else {
            answer.setFormat(AgentAnswerConstants.FORMAT_MARKDOWN);
            answer.setValue(StringUtils.defaultIfBlank(summary, "请确认后继续。"));
        }

        assistant.updateContent(answer);
        if (Objects.nonNull(assistant.getAgentConversationId())) {
            agentSseStreamPublishResolver.publish(
                    String.valueOf(assistant.getAgentConversationId()),
                    answer
            );
        }
        return Optional.of(answer);
    }

    private boolean hasSynthesizedAnswer(AgentMessageEntity assistant, String exitToolCallId) {
        return assistant.obtainMessageContents().stream()
                .filter(s -> AgentMessageContentTypeEnum.ANSWER.getValue().equals(s.getType()))
                .map(s -> CastUtils.cast(s, AnswerBlockContentMetadata.class))
                .anyMatch(a -> Objects.equals(exitToolCallId, a.getHitlToolCallId()));
    }

    private Optional<ToolCallBlockContentMetadata> findPendingExit(AgentMessageEntity assistant) {
        List<ToolCallBlockContentMetadata> tools = toolBlocks(assistant);
        return tools.stream()
                .filter(t -> isExitTool(t.getName()))
                .filter(this::isHitlPending)
                .reduce((a, b) -> b);
    }

    private boolean isExitTool(String name) {
        return ClarifyConstants.CLARIFY_EXIT.equals(name)
                || AgentAnswerConstants.PLAN_EXIT.equals(name)
                || (StringUtils.isNotBlank(name) && name.endsWith("_exit"));
    }

    private boolean isHitlPending(ToolCallBlockContentMetadata tool) {
        if (tool.getUserConfirmed() != null) {
            return false;
        }
        ToolCallState status = tool.getHitlStatus();
        return ToolCallState.PENDING.equals(status) || ToolCallState.ASKING.equals(status);
    }

    private Optional<ToolCallBlockContentMetadata> findPairedWrite(
            AgentMessageEntity assistant,
            ToolCallBlockContentMetadata exit,
            String writeName
    ) {
        List<ToolCallBlockContentMetadata> tools = toolBlocks(assistant);
        String groupId = StringUtils.defaultIfBlank(exit.getGroupId(), exit.getId());
        Optional<ToolCallBlockContentMetadata> sameGroup = tools.stream()
                .filter(t -> writeName.equals(t.getName()))
                .filter(t -> Objects.equals(StringUtils.defaultIfBlank(t.getGroupId(), t.getId()), groupId))
                .reduce((a, b) -> b);
        if (sameGroup.isPresent()) {
            return sameGroup;
        }
        return tools.stream()
                .filter(t -> writeName.equals(t.getName()))
                .reduce((a, b) -> b);
    }

    private void fillA2uiFromWrite(AnswerBlockContentMetadata answer, ToolCallBlockContentMetadata write) {
        Map<String, Object> input = parseToolInput(write);
        Map<String, Object> content = CastUtils.convertValue(
                input.get(ClarifyConstants.TOOL_PARAM_CONTENT),
                CastUtils.MAP_TYPE_REFERENCE
        );
        if (MapUtils.isEmpty(content)) {
            content = input;
        }
        List<Map<String, Object>> commands = CastUtils.convertValue(
                content.get(ClarifyConstants.CARD_COMMANDS),
                new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
                }
        );
        if (CollectionUtils.isEmpty(commands)) {
            return;
        }
        answer.setCommands(new LinkedList<>(commands));
        answer.setSurfaceId(extractSurfaceId(commands));
    }

    @SuppressWarnings("unchecked")
    private String extractSurfaceId(List<Map<String, Object>> commands) {
        for (Map<String, Object> cmd : commands) {
            Object create = cmd.get("createSurface");
            if (create instanceof Map<?, ?> map) {
                Object surfaceId = map.get("surfaceId");
                if (surfaceId != null) {
                    return String.valueOf(surfaceId);
                }
            }
            Object update = cmd.get("updateComponents");
            if (update instanceof Map<?, ?> map) {
                Object surfaceId = map.get("surfaceId");
                if (surfaceId != null) {
                    return String.valueOf(surfaceId);
                }
            }
        }
        return "clarify-surface";
    }

    private List<ToolCallBlockContentMetadata> toolBlocks(AgentMessageEntity assistant) {
        List<ToolCallBlockContentMetadata> tools = new LinkedList<>();
        for (AbstractAssistantMessageContentMetadata content : assistant.obtainMessageContents()) {
            if (AgentMessageContentTypeEnum.TOOL_CALL.getValue().equals(content.getType())) {
                tools.add(CastUtils.cast(content, ToolCallBlockContentMetadata.class));
            }
        }
        return tools;
    }

    private Map<String, Object> parseToolInput(ToolCallBlockContentMetadata tool) {
        if (StringUtils.isBlank(tool.getValue())) {
            return Map.of();
        }
        try {
            Map<String, Object> map = SystemException.convertSupplier(
                    () -> CastUtils.getObjectMapper().readValue(tool.getValue(), CastUtils.MAP_TYPE_REFERENCE)
            );
            return map == null ? Map.of() : map;
        } catch (RuntimeException ex) {
            return Map.of();
        }
    }
}
