package io.github.loncra.basic.service.ai.server.controller.hub;

import io.github.loncra.basic.service.ai.server.domain.body.UserPluginInstallRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.UserPluginInstallResult;
import io.github.loncra.basic.service.ai.server.service.hub.AiUserPluginInstallService;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户广场插件安装
 *
 * @author maurice.chen
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("ai/user/plugin/install")
@Plugin(
        name = "插件安装",
        id = "ai_user_plugin_install",
        parent = "agent",
        authority = "isFullyAuthenticated()",
        type = ResourceTypeEnum.RESOURCE_TOOL_TYPE,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class AiUserPluginInstallController {

    private final AiUserPluginInstallService aiUserPluginInstallService;

    @PostMapping
    @OperationDataTrace("安装广场插件")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<UserPluginInstallResult> install(
            @Valid
            @RequestBody
            UserPluginInstallRequestBody body,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return RestResult.ofSuccess("安装成功", aiUserPluginInstallService.install(body, token));
    }

    @PostMapping("my")
    @PreAuthorize("isFullyAuthenticated()")
    public List<UserPluginInstallResult> my(
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return aiUserPluginInstallService.find(token);
    }

    @DeleteMapping("/{id:\\d+}")
    @OperationDataTrace("卸载广场插件")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<Void> uninstall(
            @PathVariable
            Long id,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        aiUserPluginInstallService.uninstall(id, token);
        return RestResult.of("卸载成功");
    }
}
