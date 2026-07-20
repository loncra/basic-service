package io.github.loncra.basic.service.ai.server.controller.agent;


import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.service.agent.AgentManager;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.HttpRequestParameterMapUtils;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("agent")
@Plugin(
        name = "智能体",
        id = "agent",
        authority = "isFullyAuthenticated()",
        type = ResourceTypeEnum.RESOURCE_TOOL_TYPE,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class AgentController {

    private final AgentManager agentManager;

    @PostMapping
    public AgentChatResponseBody chat(
            @RequestBody
            AgentChatRequestBody body,
            @CurrentSecurityContext SecurityContext securityContext
    ){
        /*String textMessageMetadata = TextMessageMetadata.ofString(body.getContent());
        ReActAgent agent = ReActAgent.builder()
                        .model(body.getModel()) // 底层由 ModelRegistry.resolve(modelId) 解析
                        .build();

        return agent.streamEvents(textMessageMetadata).subscribe(System.out::println);*/
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return agentManager.chat(body, token);
    }
}
