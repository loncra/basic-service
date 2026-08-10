package io.github.loncra.basic.service.ai.server.domain.clarify;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ClarifySessionState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private boolean active;

    private String targetTool;

    private Map<String, Object> card = new LinkedHashMap<>();

    private boolean writeDone;

    private int rounds;

    private Map<String, Integer> callCounts = new LinkedHashMap<>();

    private boolean dimensionsSatisfied;

    private Map<String, Object> answers = new LinkedHashMap<>();
}
