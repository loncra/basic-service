package io.github.loncra.basic.service.message.server.config;

import io.github.loncra.basic.service.message.server.resolver.support.chat.call.LiveKitCallMediaServerResolver;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.domain.metadata.CloudSecretMetadata;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;

@Data
@Component
@ConfigurationProperties("loncra.basic-service.message.app.chat.call")
public class UserChatCallConfig {

    private TimeProperties privateCallingExpirationTime = TimeProperties.ofMinutes(1);

    private TimeProperties groupCallingExpirationTime = TimeProperties.ofMinutes(5);

    private TimeProperties meetingCallingExpirationTime = TimeProperties.ofMinutes(30);

    private TimeProperties delayCompletedTime = TimeProperties.ofSeconds(30);

    private String mediaServer = LiveKitCallMediaServerResolver.DEFAULT_TYPE;

    private LiveKitConfig livekit = new  LiveKitConfig();

    @Data
    public static class LiveKitConfig implements Serializable {

        @Serial
        private static final long serialVersionUID = 8419668383270093700L;

        private String host = "http://10.20.5.152:7880";

        private CloudSecretMetadata secret = new CloudSecretMetadata();
    }
}
