package io.github.loncra.basic.service.ai.server.resolver.channel;

import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentChannelResolver;
import io.github.loncra.framework.commons.exception.SystemException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AgentChannelResolverFactory {

    private final List<AgentChannelResolver> channelResolvers;

    public AgentChannelResolver getChannelResolver(
            ExecutionChannelEnum channel
    ) {
        Objects.requireNonNull(channel, "执行通道不能为空");
        return channelResolvers.stream()
                .filter(s -> s.getChannel().equals(channel))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到通道 [" + channel + "] 的解析器"));
    }

}
