package io.github.loncra.basic.service.commons.domain.metadata.chat;

import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    public static List<InstructionMessageMetadata> ofList(List<Map<String, Object>> content) {
        if (CollectionUtils.isEmpty(content)) {
            return List.of();
        }
        List<InstructionMessageMetadata> result = new LinkedList<>();
        for (Map<String, Object> block : content) {
            if (block == null) {
                continue;
            }
            if (!AbstractCustomMessageMetadata.DEFAULT_TYPE_VALUE.equals(
                    block.getOrDefault(TypeIdNameMetadata.TYPE_FIELD_NAME, StringUtils.EMPTY))) {
                continue;
            }
            if (!DEFAULT_SLOT_KIND.equals(
                    block.getOrDefault(AbstractCustomMessageMetadata.SLOT_KIND_KEY, StringUtils.EMPTY))) {
                continue;
            }
            try {
                InstructionMessageMetadata instruction = CastUtils.convertValue(block, InstructionMessageMetadata.class);
                if (instruction != null) {
                    result.add(instruction);
                }
            } catch (RuntimeException ignored) {
                // 非法 instruction 忽略，聊天不失败
            }
        }
        return result;
    }
}
