package io.github.loncra.basic.service.ai.server.controller.agent;


import io.github.loncra.basic.service.ai.server.domain.body.AgentChatBasicResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentResumeRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
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
import io.github.loncra.framework.spring.web.device.DeviceUtils;
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
        sources = {ResourceSourceEnum.CONSOLE_SOURCE_VALUE,ResourceSourceEnum.PERSONAL_SOURCE_VALUE, ResourceSourceEnum.ENTERPRISE_SOURCE_VALUE}
)
public class AgentController {

    private final AgentManager agentManager;

    @PostMapping
    public AgentChatResponseBody chat(
            @RequestBody
            AgentChatRequestBody body,
            @CurrentSecurityContext SecurityContext securityContext,
            HttpServletRequest request
    ){
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return agentManager.chat(
                body,
                token,
                request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME)
        );
    }

    @PutMapping
    public AgentChatBasicResponseBody resume(
            @RequestBody
            AgentResumeRequestBody body,
            @CurrentSecurityContext SecurityContext securityContext,
            HttpServletRequest request
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return agentManager.resume(
                body,
                token,
                request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME)
        );
    }

    @DeleteMapping("/{assistantMessageId:\\d+}")
    public RestResult<Long> interrupt(
            @PathVariable
            Long assistantMessageId,
            @RequestParam
            ExecutionChannelEnum channel,
            @CurrentSecurityContext
            SecurityContext securityContext,
            HttpServletRequest request
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        Long sessionId = agentManager.interrupt(
                assistantMessageId,
                channel,
                token,
                request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME)
        );
        return RestResult.of("中断 [" + sessionId + "] 会话成功");
    }

    @PostMapping(value = "/stream/{assistantMessageId:\\d+}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(
            @PathVariable
            Long assistantMessageId,
            @RequestParam
            ExecutionChannelEnum channel,
            @RequestParam(required = false, defaultValue = "true")
            boolean loadHistory,
            @CurrentSecurityContext SecurityContext securityContext,
            HttpServletRequest request
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return agentManager.stream(
                assistantMessageId,
                channel,
                loadHistory,
                token,
                request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME)
        );
    }

    @PostMapping("message/history/{conversationId:\\d+}")
    public Page<AgentMessageEntity> history(
            PageRequest pageRequest,
            HttpServletRequest request,
            @RequestParam
            ExecutionChannelEnum channel,
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
                channel,
                conversationId,
                totalPage,
                token,
                request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME)
        );
    }

    @GetMapping("message/positioning/page/number/{conversationId:\\d+}/{messageId:\\d+}/{pageSize:\\d+}")
    public RestResult<Integer> positioningPageNumber(
            @PathVariable
            Long conversationId,
            @PathVariable
            Long messageId,
            @PathVariable
            int pageSize,
            @RequestParam
            ExecutionChannelEnum channel,
            HttpServletRequest request
    ) {
        return RestResult.ofSuccess(
                agentManager.positioningMessagePageNumber(
                        channel,
                        conversationId,
                        messageId,
                        pageSize,
                        request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME)
                )
        );
    }

    @DeleteMapping("conversation")
    public RestResult<Void> deleteConversation(
            @RequestParam
            List<Integer> ids,
            @RequestParam
            ExecutionChannelEnum channel,
            @CurrentSecurityContext
            SecurityContext securityContext,
            HttpServletRequest request
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        agentManager.deleteConversation(
                ids,
                channel,
                token,
                request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME)
        );
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }
}
