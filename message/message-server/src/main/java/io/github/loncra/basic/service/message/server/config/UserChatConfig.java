package io.github.loncra.basic.service.message.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loncra.basic-service.message.app.chat")
public class UserChatConfig {

    private String joinRoomText = "{0},已加入了房间";

    private Integer conversationCoverLimit = 3;

    private Integer conversationNameLimit = 3;
}
