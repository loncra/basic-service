package io.github.loncra.basic.service.ai.server.controller.agent;


import io.github.loncra.basic.service.ai.server.domain.body.AgentChatBasicResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentResumeRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.service.agent.AgentManager;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.HttpRequestParameterMapUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

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
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return agentManager.chat(body, token);
    }

    @PostMapping(value = "/stream/{assistantId:\\d+}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(
            @PathVariable
            Long assistantId,
            @CurrentSecurityContext SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return agentManager.stream(assistantId, token);
    }

    @PostMapping("message/history/{conversationId:\\d+}")
    public Page<AgentMessageEntity> history(
            PageRequest pageRequest,
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "false")
            boolean totalPage,
            @PathVariable
            Long conversationId,
            @CurrentSecurityContext SecurityContext securityContext
    ) {
        MultiValueMap<String, String> parameter = HttpRequestParameterMapUtils.castMapToMultiValueMap(request.getParameterMap());
        MultiValueMap<String, Object> filter = new LinkedMultiValueMap<>();
        parameter.forEach(filter::addAll);
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return agentManager.histories(
                pageRequest,
                filter,
                conversationId,
                totalPage,
                token
        );
    }

    @GetMapping("message/positioning/page/number/{conversationId:\\d+}/{messageId:\\d+}/{pageSize:\\d+}")
    public RestResult<Integer> positioningPageNumber(
            @PathVariable
            Long conversationId,
            @PathVariable
            Long messageId,
            @PathVariable
            int pageSize
    ) {
        return RestResult.ofSuccess(
                agentManager.positioningMessagePageNumber(conversationId, messageId, pageSize)
        );
    }

    @PutMapping
    public AgentChatBasicResponseBody resume(
            @RequestBody
            AgentResumeRequestBody body,
            @CurrentSecurityContext SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return agentManager.resume(body, token);
    }

    @DeleteMapping("conversation")
    public RestResult<Void> deleteConversation(
            @RequestParam
            List<Integer> ids,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        agentManager.deleteConversation(ids, token);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }
}
