package io.github.loncra.basic.service.message.server.domain.metadata.chat;

import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InstructionMessageMetadata extends AbstractCustomMessageMetadata{

    public static final String DEFAULT_SLOT_KIND = "instruction";

    private String prefix;

    private IdValueMetadata<String, String> value;

    @Override
    public String getSlotKind() {
        return DEFAULT_SLOT_KIND;
    }
}
