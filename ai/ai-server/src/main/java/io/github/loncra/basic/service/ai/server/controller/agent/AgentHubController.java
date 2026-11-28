package io.github.loncra.basic.service.ai.server.controller.agent;

import io.github.loncra.basic.service.ai.server.domain.body.UserPluginInstallResult;
import io.github.loncra.basic.service.ai.server.domain.body.UserPluginRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.UserPluginStatusRequestBody;
import io.github.loncra.basic.service.ai.server.service.agent.AgentHubService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.web.device.DeviceUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("agent/hub")
public class AgentHubController {

    private final AgentHubService agentHubService;

    @PostMapping
    @OperationDataTrace("安装广场插件")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<List<UserPluginInstallResult>> install(
            @Valid
            @RequestBody
            UserPluginRequestBody body,
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME)
            String deviceIdentified
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return RestResult.ofSuccess("安装成功", agentHubService.install(body, token, deviceIdentified));
    }

    @PostMapping("my")
    @PreAuthorize("isFullyAuthenticated()")
    public List<UserPluginInstallResult> my(
            @RequestParam(required = false)
            String workspaceId,
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME)
            String deviceIdentified
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return agentHubService.findInstalls(token, deviceIdentified, workspaceId);
    }

    @PutMapping("status")
    @OperationDataTrace("启用或禁用广场插件")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<UserPluginInstallResult> updateStatus(
            @Valid
            @RequestBody
            UserPluginStatusRequestBody body,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return RestResult.ofSuccess("操作成功", agentHubService.updateInstallStatus(body, token));
    }

    @DeleteMapping
    @OperationDataTrace("卸载广场插件")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<Void> uninstall(
            @RequestBody
            UserPluginRequestBody body,
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME)
            String deviceIdentified
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        agentHubService.uninstall(body, token, deviceIdentified);
        return RestResult.of("卸载成功");
    }
}
