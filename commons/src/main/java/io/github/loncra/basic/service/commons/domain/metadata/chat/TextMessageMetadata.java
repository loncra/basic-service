package io.github.loncra.basic.service.commons.domain.metadata.chat;

import io.github.loncra.basic.service.commons.domain.ChatMessageContent;
import io.github.loncra.basic.service.commons.enumerate.ChatMessageContentTypeEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class TextMessageMetadata implements ChatMessageContent {

    @NonNull
    String value;

    @Override
    public String getType() {
        return ChatMessageContentTypeEnum.TEXT.getValue();
    }

    public static List<TextMessageMetadata> ofList(List<Map<String, Object>> content) {
        return content.stream()
                .filter(s -> Objects.equals(s.get(TypeIdNameMetadata.TYPE_FIELD_NAME), ChatMessageContentTypeEnum.TEXT.getValue()))
                .map(s -> CastUtils.convertValue(s,  TextMessageMetadata.class))
                .toList();
    }

    public static String ofString(List<Map<String, Object>> content) {
        if (CollectionUtils.isEmpty(content)) {
            return StringUtils.EMPTY;
        }
        return ofList(content).stream().map(TextMessageMetadata::getValue).collect(Collectors.joining(StringUtils.EMPTY));
    }
}
