package io.github.loncra.basic.service.ai.server.domain.clarify;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public class ClarifyToolPolicy implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String toolName;

    private List<String> requiredDimensions = new LinkedList<>();

    private int maxCallsPerTurn = 3;

    private int maxClarifyRounds = 3;

    private String formTitle;

    private List<ClarifyFieldTemplate> fields = new LinkedList<>();

    /**
     * 原始 formTemplate（含 title/fields），便于回传模型约束
     */
    private Map<String, Object> formTemplate = new LinkedHashMap<>();
}
