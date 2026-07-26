package io.github.loncra.basic.service.ai.server.config;

import io.github.loncra.framework.commons.TimeProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Data
@Component
@NoArgsConstructor
@EqualsAndHashCode
@ConfigurationProperties("loncra.basic-service.ai.app.conversation")
public class ConversationConfig {

    private String defaultWorkspaceName = "default";

    private String newConversation = "新话题";

    private boolean enabled = true;

    private String generatePrompt = """
            请根据以下对话内容，生成一个简短标题（5-10字），直接输出标题，不要加任何解释或前缀。
            [问题] : {0}
            [应答] : {1}
            ### 要求:
            1. 必须根据聊天内容的语言自行转换语言输出标题
            2. 标题必须涵盖上下文的中心思想
            """;

    private TimeProperties generateTimeout = TimeProperties.of(15, TimeUnit.SECONDS);
}
