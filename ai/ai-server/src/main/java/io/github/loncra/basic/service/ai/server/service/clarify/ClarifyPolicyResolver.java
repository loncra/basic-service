package io.github.loncra.basic.service.ai.server.service.clarify;

import io.github.loncra.basic.service.ai.server.constants.ClarifyConstants;
import io.github.loncra.basic.service.ai.server.domain.clarify.ClarifyFieldTemplate;
import io.github.loncra.basic.service.ai.server.domain.clarify.ClarifyToolPolicy;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiMcpPackageEntity;
import io.github.loncra.basic.service.ai.server.service.hub.AiMcpPackageService;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 从 {@link AiMcpPackageEntity#getMetadata()} 解析 clarify 策略
 */
@Component
@RequiredArgsConstructor
public class ClarifyPolicyResolver {

    private final AiMcpPackageService aiMcpPackageService;

    public Optional<ClarifyToolPolicy> findToolPolicy(String toolName) {
        if (StringUtils.isBlank(toolName)) {
            return Optional.empty();
        }
        List<AiMcpPackageEntity> packages = aiMcpPackageService.findSystemMcpPackage();
        for (AiMcpPackageEntity mcpPackage : packages) {
            Optional<ClarifyToolPolicy> policy = parseToolPolicy(mcpPackage, toolName);
            if (policy.isPresent()) {
                return policy;
            }
        }
        return Optional.empty();
    }

    public boolean isControlledTool(String toolName) {
        return findToolPolicy(toolName).isPresent();
    }

    public Set<String> findAllControlledToolNames() {
        Set<String> names = new LinkedHashSet<>();
        for (AiMcpPackageEntity mcpPackage : aiMcpPackageService.findSystemMcpPackage()) {
            Map<String, Object> clarify = obtainClarifyRoot(mcpPackage);
            if (MapUtils.isEmpty(clarify)) {
                continue;
            }
            Object enabled = clarify.get(ClarifyConstants.POLICY_ENABLED);
            if (Objects.nonNull(enabled) && !Boolean.parseBoolean(String.valueOf(enabled))) {
                continue;
            }
            Map<String, Object> tools = CastUtils.convertValue(
                    clarify.get(ClarifyConstants.POLICY_TOOLS),
                    CastUtils.MAP_TYPE_REFERENCE
            );
            if (MapUtils.isEmpty(tools)) {
                continue;
            }
            names.addAll(tools.keySet());
        }
        return names;
    }

    private Optional<ClarifyToolPolicy> parseToolPolicy(AiMcpPackageEntity mcpPackage, String toolName) {
        Map<String, Object> clarify = obtainClarifyRoot(mcpPackage);
        if (MapUtils.isEmpty(clarify)) {
            return Optional.empty();
        }
        Object enabled = clarify.get(ClarifyConstants.POLICY_ENABLED);
        if (Objects.nonNull(enabled) && !Boolean.parseBoolean(String.valueOf(enabled))) {
            return Optional.empty();
        }
        Map<String, Object> tools = CastUtils.convertValue(
                clarify.get(ClarifyConstants.POLICY_TOOLS),
                CastUtils.MAP_TYPE_REFERENCE
        );
        if (MapUtils.isEmpty(tools) || !tools.containsKey(toolName)) {
            return Optional.empty();
        }
        Map<String, Object> toolCfg = CastUtils.convertValue(tools.get(toolName), CastUtils.MAP_TYPE_REFERENCE);
        if (MapUtils.isEmpty(toolCfg)) {
            return Optional.empty();
        }
        ClarifyToolPolicy policy = new ClarifyToolPolicy();
        policy.setToolName(toolName);
        List<String> dims = CastUtils.convertValue(
                toolCfg.get(ClarifyConstants.POLICY_REQUIRED_DIMENSIONS),
                new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
                }
        );
        if (CollectionUtils.isNotEmpty(dims)) {
            policy.setRequiredDimensions(new LinkedList<>(dims));
        }
        Object maxCalls = toolCfg.get(ClarifyConstants.POLICY_MAX_CALLS_PER_TURN);
        if (Objects.nonNull(maxCalls)) {
            policy.setMaxCallsPerTurn(Integer.parseInt(String.valueOf(maxCalls)));
        }
        Object maxRounds = toolCfg.get(ClarifyConstants.POLICY_MAX_CLARIFY_ROUNDS);
        if (Objects.nonNull(maxRounds)) {
            policy.setMaxClarifyRounds(Integer.parseInt(String.valueOf(maxRounds)));
        }
        Map<String, Object> formTemplate = CastUtils.convertValue(
                toolCfg.get(ClarifyConstants.POLICY_FORM_TEMPLATE),
                CastUtils.MAP_TYPE_REFERENCE
        );
        if (MapUtils.isNotEmpty(formTemplate)) {
            policy.setFormTemplate(formTemplate);
            policy.setFormTitle(Objects.toString(formTemplate.get(ClarifyConstants.TEMPLATE_TITLE), null));
            List<Map<String, Object>> fieldMaps = CastUtils.convertValue(
                    formTemplate.get(ClarifyConstants.TEMPLATE_FIELDS),
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
                    }
            );
            if (CollectionUtils.isNotEmpty(fieldMaps)) {
                List<ClarifyFieldTemplate> fields = new LinkedList<>();
                for (Map<String, Object> fieldMap : fieldMaps) {
                    ClarifyFieldTemplate field = new ClarifyFieldTemplate();
                    field.setKey(Objects.toString(fieldMap.get(ClarifyConstants.FIELD_KEY), null));
                    field.setWidget(Objects.toString(fieldMap.get(ClarifyConstants.FIELD_WIDGET), ClarifyConstants.WIDGET_INPUT));
                    field.setLabel(Objects.toString(fieldMap.get(ClarifyConstants.FIELD_LABEL), field.getKey()));
                    field.setRequired(Boolean.parseBoolean(String.valueOf(fieldMap.getOrDefault(ClarifyConstants.FIELD_REQUIRED, false))));
                    List<String> options = CastUtils.convertValue(
                            fieldMap.get(ClarifyConstants.FIELD_OPTIONS),
                            new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
                            }
                    );
                    if (CollectionUtils.isNotEmpty(options)) {
                        field.setOptions(new LinkedList<>(options));
                    }
                    fields.add(field);
                }
                policy.setFields(fields);
            }
        }
        return Optional.of(policy);
    }

    private Map<String, Object> obtainClarifyRoot(AiMcpPackageEntity mcpPackage) {
        if (Objects.isNull(mcpPackage) || MapUtils.isEmpty(mcpPackage.getMetadata())) {
            return Collections.emptyMap();
        }
        return CastUtils.convertValue(
                mcpPackage.getMetadata().get(ClarifyConstants.METADATA_CLARIFY_KEY),
                CastUtils.MAP_TYPE_REFERENCE
        );
    }
}
