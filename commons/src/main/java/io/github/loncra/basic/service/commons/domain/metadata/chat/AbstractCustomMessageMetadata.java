package io.github.loncra.basic.service.commons.domain.metadata.chat;

import io.github.loncra.basic.service.commons.domain.ChatMessageContent;
import io.github.loncra.framework.commons.id.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractCustomMessageMetadata extends IdEntity<String> implements ChatMessageContent {

    public static final String DEFAULT_TYPE_VALUE = "custom";

    public static final String SLOT_KIND_KEY = "slotKind";

    private Map<String, Object> metadata = new LinkedHashMap<>();

    @Override
    public String getType() {
        return DEFAULT_TYPE_VALUE;
    }

    abstract public String getSlotKind();
}
