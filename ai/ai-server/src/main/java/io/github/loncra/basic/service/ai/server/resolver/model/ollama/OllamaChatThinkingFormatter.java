package io.github.loncra.basic.service.ai.server.resolver.model.ollama;

import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.ThinkingBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.extensions.model.ollama.dto.OllamaMessage;
import io.agentscope.extensions.model.ollama.dto.OllamaResponse;
import io.agentscope.extensions.model.ollama.formatter.OllamaChatFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OllamaChatThinkingFormatter extends OllamaChatFormatter {
    @Override
    public ChatResponse parseResponse(
            OllamaResponse response,
            Instant startTime
    ) {
        ChatResponse base = super.parseResponse(response, startTime);
        OllamaMessage msg = response == null ? null : response.getMessage();
        String thinking = msg == null ? null : msg.getThinking();
        if (StringUtils.isBlank(thinking)) {
            return base;
        }
        List<ContentBlock> blocks = new ArrayList<>();
        // 流式每帧 thinking 是增量片段，按帧建 ThinkingBlock 即可（和 TextBlock 一样）
        blocks.add(ThinkingBlock.builder().thinking(thinking).build()); // 若 API 是 thinking()/content() 改成对应方法
        /*if (base.getContent() != null) {
            blocks.addAll(base.getContent());
        }*/
        return ChatResponse.builder()
                .id(base.getId())
                .content(blocks)
                .usage(base.getUsage())
                .metadata(base.getMetadata())
                .finishReason(base.getFinishReason())
                .build();
    }
}
