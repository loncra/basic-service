package io.github.loncra.basic.service.ai.server.service.agent;

import io.github.loncra.basic.service.message.api.domian.metadata.SocketSessionMetadata;
import io.github.loncra.basic.service.message.api.service.SocketServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentSocketSessionResolver {

    private final SocketServiceClient socketServiceClient;

    public SocketSessionMetadata getSession(String principal, String deviceIdentified) {
        if (StringUtils.isBlank(principal) || StringUtils.isBlank(deviceIdentified)) {
            return null;
        }

        try {
            SocketSessionMetadata session = socketServiceClient.getSocketSession(deviceIdentified);
            if (Objects.isNull(session) || !session.isOnline()) {
                return null;
            }
            if (!StringUtils.equals(session.getPrincipal(), principal)) {
                return null;
            }
            return session;
        }
        catch (Exception e) {
            log.warn("查询客户端会话失败, deviceIdentified={}", deviceIdentified, e);
            return null;
        }
    }
}
