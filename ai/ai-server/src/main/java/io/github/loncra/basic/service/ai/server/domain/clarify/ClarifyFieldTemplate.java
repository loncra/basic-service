package io.github.loncra.basic.service.ai.server.domain.clarify;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Data
public class ClarifyFieldTemplate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String key;

    private String widget;

    private String label;

    private boolean required;

    private List<String> options = new LinkedList<>();
}
