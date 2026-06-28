package io.github.loncra.basic.service.message.server.domain.metadata.chat;

import io.github.loncra.basic.service.message.server.domain.UserChatMessageContent;
import io.github.loncra.framework.commons.id.IdEntity;

public abstract class AbstractCustomMessageMetadata extends IdEntity<String> implements UserChatMessageContent{

    public static final String DEFAULT_TYPE_VALUE = "custom";

    public static final String SLOT_KIND_KEY = "slotKind";

    @Override
    public String getType() {
        return DEFAULT_TYPE_VALUE;
    }

    abstract protected String getSlotKind();
}
