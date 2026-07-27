package io.github.loncra.basic.service.ai.server.resolver.model.ollama;

import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.ThinkingBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.extensions.model.ollama.dto.OllamaResponse;
import io.agentscope.extensions.model.ollama.formatter.OllamaChatFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class OllamaChatThinkingFormatter extends OllamaChatFormatter {
    @Override
    public ChatResponse parseResponse(
            OllamaResponse response,
            Instant startTime
    ) {

        ChatResponse base = super.parseResponse(response, startTime);

        if (Objects.isNull(response)) {
            return base;
        }

        if (StringUtils.isNotBlank(response.getMessage().getThinking())) {
            List<ContentBlock> blocks = new ArrayList<>();
            blocks.add(ThinkingBlock.builder().thinking(response.getMessage().getThinking()).build());
            return ChatResponse.builder()
                    .id(base.getId())
                    .content(blocks)
                    .usage(base.getUsage())
                    .metadata(base.getMetadata())
                    .finishReason(base.getFinishReason())
                    .build();
        } else {
            return base;
        }
    }
}
