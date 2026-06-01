package io.github.loncra.basic.service.auth.server.controller;

import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.domain.AbstractPlatformUser;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.WechatAuthenticationEntity;
import io.github.loncra.basic.service.auth.server.enumerate.oauth.RegisteredClientScopeEnum;
import io.github.loncra.basic.service.auth.server.security.handler.JsonLogoutSuccessHandler;
import io.github.loncra.basic.service.auth.server.service.AbstractAuthorizationService;
import io.github.loncra.basic.service.auth.server.service.WechatAuthenticationService;
import io.github.loncra.basic.service.auth.server.service.resource.plugin.DelegatingPluginResourceService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.HttpRequestParameterMapUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdNameMetadata;
import io.github.loncra.framework.commons.id.metadata.IdNameValueMetadata;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.commons.tree.TreeUtils;
import io.github.loncra.framework.security.audit.Auditable;
import io.github.loncra.framework.security.audit.IdAuditEvent;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import io.github.loncra.framework.spring.security.core.authentication.handler.JsonAuthenticationSuccessResponse;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.wechat.domain.WechatUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 认证管理
 *
 * @author maurice.chen
 */
@Controller
@RequiredArgsConstructor
public class AuthRootController {

    private final JsonLogoutSuccessHandler jsonLogoutSuccessHandler;

    private final RegisteredClientRepository registeredClientRepository;

    private final AbstractAuthorizationService<AbstractBasicSystemUser> authorizationService;

    private final List<JsonAuthenticationSuccessResponse> successResponses;

    private final ObjectProvider<WechatAuthenticationService> wechatAuthenticationService;

    private final DelegatingPluginResourceService delegatingPluginResourceService;

    /**
     * 登录预处理
     *
     * @param request http servlet request
     *
     * @return REST 响应结果
     */
    @ResponseBody
    @GetMapping("prepare")
    public RestResult<Object> prepare(HttpServletRequest request) {
        RestResult<Object> result = RestResult.ofSuccess(jsonLogoutSuccessHandler.createUnauthorizedResult(request).getData());
        if (CollectionUtils.isNotEmpty(successResponses)) {
            successResponses.forEach(s -> s.setting(result, request));
        }
        return result;
    }

    /**
     * 用户登录
     *
     * @return REST 响应结果
     */
    @ResponseBody
    @GetMapping("login")
    public RestResult<Object> login(HttpServletRequest request) {
        return prepare(request);
    }

    /**
     * 登录成功后跳转的连接，直接获取当前用户
     *
     * @param securityContext 安全上下文
     *
     * @return REST 响应结果
     */
    @ResponseBody
    @GetMapping("user")
    @PreAuthorize("isAuthenticated()")
    public RestResult<Object> getPrincipal(
            @CurrentSecurityContext
            SecurityContext securityContext,
            HttpServletRequest request
    ) {
        RestResult<Object> result = RestResult.ofSuccess(securityContext.getAuthentication());
        if (CollectionUtils.isNotEmpty(successResponses)) {
            successResponses.forEach(s -> s.setting(result, request));
        }
        return result;
    }

    /**
     * 获取当前用户资源
     *
     * @param securityContext 安全上下文
     * @param mergeTree       是否合并树形 true，是 否则 false
     *
     * @return 资源实体集合
     */
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    @GetMapping("principalResources")
    public List<ResourceEntity> getPrincipalResources(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestParam(required = false)
            List<String> types,
            @RequestParam(required = false)
            boolean mergeTree
    ) {

        Assert.isTrue(
                AuditAuthenticationToken.class.isAssignableFrom(securityContext.getAuthentication().getClass()),
                "当前 Authentication 非 AuditAuthenticationToken 实例，无法需改个人登录密码"
        );
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());

        List<ResourceSourceEnum> sourceContains = Collections.singletonList(
                NameEnum.ofEnum(ResourceSourceEnum.class, token.getType())
        );

        List<ResourceEntity> resourceList = authorizationService.getSystemUserResource(
                token,
                types.stream().map(v -> ValueEnum.ofEnum(ResourceTypeEnum.class, v)).toList(),
                sourceContains
        );

        if (mergeTree) {
            return TreeUtils.buildGenericTree(resourceList);
        }
        else {
            return resourceList;
        }
    }

    @ResponseBody
    @GetMapping(value = "/oauth2/consent")
    public RestResult<Map<String, Object>> consent(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestParam(OAuth2ParameterNames.CLIENT_ID)
            String clientId,
            @RequestParam(OAuth2ParameterNames.SCOPE)
            String scope,
            @RequestParam(OAuth2ParameterNames.STATE)
            String state
    ) {
        Map<String, Object> result = new LinkedHashMap<>();

        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        Assert.notNull(registeredClient, "找不到 ID 为 [" + clientId + "] 的商户客户端信息");
        Authentication authentication = securityContext.getAuthentication();

        List<String> requestedScopes = new LinkedList<>(Arrays.asList(StringUtils.splitByWholeSeparator(scope, StringUtils.SPACE)));

        List<RegisteredClientScopeEnum.Description> scopes = requestedScopes.stream()
                .map(s -> ValueEnum.ofEnum(RegisteredClientScopeEnum.class, s, true))
                .filter(Objects::nonNull)
                .map(RegisteredClientScopeEnum::toDescription)
                .collect(Collectors.toList());

        result.put(OAuth2ParameterNames.CLIENT_ID, clientId);
        result.put(OAuth2ParameterNames.STATE, state);
        result.put(OAuth2ParameterNames.SCOPE, scopes);
        result.put(IdAuditEvent.PRINCIPAL_FIELD_NAME, authentication.getDetails());

        return RestResult.ofSuccess(result);
    }

    /**
     * 更新系统用户登录密码
     *
     * @param securityContext 安全上下文
     * @param oldPassword     旧密码
     * @param newPassword     新密码
     */
    @ResponseBody
    @OperationDataTrace
    @PutMapping("user/password/update")
    @PreAuthorize("isFullyAuthenticated()")
    @Plugin(name = "修改个人登录密码", parent = "authority")
    public RestResult<Void> updatePassword(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestParam
            String oldPassword,
            @RequestParam
            String newPassword
    ) {

        Assert.isTrue(
                AuditAuthenticationToken.class.isAssignableFrom(securityContext.getAuthentication().getClass()),
                "当前 Authentication 非 AuditAuthenticationToken 实例，无法需改个人登录密码"
        );
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        authorizationService.updatePassword(token, oldPassword, newPassword);
        return RestResult.of("修改密码成功");
    }

    /**
     * 更新系统用户登录密码
     *
     * @param type 用户类型
     * @param id   用户 id
     */
    @ResponseBody
    @OperationDataTrace
    @PutMapping("user/password/admin/reset")
    @Plugin(name = "重置用户密码", parent = "authority")
    @PreAuthorize("hasAuthority('perms[auth_server_system_user:admin_reset_password]')")
    public RestResult<Object> adminRestPassword(
            String type,
            String id
    ) {
        String newPassword = authorizationService.adminRestPassword(type, id);
        return RestResult.ofSuccess("重置密码成功", (Object) newPassword);
    }

    /**
     * 更具手机号码创建系统用户
     *
     * @param phoneNumber 手机号码
     * @param type        用户类型
     *
     * @return REST 响应结果
     */
    @ResponseBody
    @PutMapping("system/user/create/phone")
    public AbstractBasicSystemUser createSystemUserByPhoneNumber(
            @RequestParam
            String phoneNumber,
            @RequestParam
            String type
    ) {
        return authorizationService.getSystemUserAuthorizationResolver(type, true)
                .createByPhoneNumber(phoneNumber);
    }

    /**
     * 获取系统用户
     *
     * @param systemName 用户系统形成
     *
     * @see AbstractPlatformUser#getSystemName()
     *
     * @return REST 响应结果
     */
    @ResponseBody
    @PostMapping("system/user/{systemName}")
    public AbstractBasicSystemUser systemUser(
            @PathVariable
            String systemName
    ) {
        TypeIdNameMetadata metadata = TypeIdNameMetadata.ofPrincipalString(systemName);
        return authorizationService.getSystemUserAuthorizationResolver(metadata.getType(), true)
                .getByIdentity(metadata.getId());
    }


    /**
     * 查询系统用户
     *
     * @param pageRequest 分页请求
     * @param request     http servlet request
     *
     * @return REST 响应结果
     */
    @ResponseBody
    @PostMapping("system/users")
    @PreAuthorize("isAuthenticated()")
    public Object systemUsers(
            PageRequest pageRequest,
            HttpServletRequest request,
            @RequestParam(required = false)
            List<String> ignoreTypes,
            @RequestParam(required = false, defaultValue = "false")
            boolean idNameValueMetadata
    ) {
        if (CollectionUtils.isEmpty(ignoreTypes)) {
            ignoreTypes = new LinkedList<>();
        }
        Map<IdNameMetadata, List<AbstractBasicSystemUser>> result = authorizationService.findSystemUser(pageRequest, ignoreTypes, request);
        if (idNameValueMetadata) {
            List<IdNameValueMetadata<String, List<AbstractBasicSystemUser>>> metadataList = new LinkedList<>();
            result.forEach((k, v) -> metadataList.add(new IdNameValueMetadata<>(k.getId(), k.getName(), v)));
            return metadataList;
        }
        else {
            Map<String, Object> map = new LinkedHashMap<>();
            result.forEach((k, v) -> map.put(k.getName(), v));
            return map;
        }
    }

    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    @PostMapping("system/users/find/{type}")
    public List<AbstractBasicSystemUser> findSystemUser(
            @PathVariable(required = false) String type,
            HttpServletRequest request
    ) {
        MultiValueMap<String, Object> filter = new LinkedMultiValueMap<>();
        HttpRequestParameterMapUtils.castMapToMultiValueMap(request.getParameterMap())
                .forEach((k, v) -> filter.put(k, new LinkedList<>(v)));
        if (StringUtils.isEmpty(type)) {
            return authorizationService.getSystemUserAuthorizationResolvers()
                    .stream()
                    .flatMap(s -> s.findPage(PageRequest.of(-1), filter).getElements().stream())
                    .toList();
        }
        else {
            return authorizationService.getSystemUserAuthorizationResolver(type, true)
                    .findPage(PageRequest.of(-1), new LinkedMultiValueMap<>(filter))
                    .getElements();
        }
    }

    @ResponseBody
    @GetMapping("systemUserTypes")
    @PreAuthorize("isAuthenticated()")
    public List<IdNameMetadata> getSystemUserTypes() {
        return authorizationService.getSystemUserTypes();
    }

    @ResponseBody
    @GetMapping("wechatAuthentication")
    @ConditionalOnProperty(prefix = "loncra.framework.wechat", value = "enabled", matchIfMissing = true)
    public WechatAuthenticationEntity getWechatAuthentication(String principal) {
        return wechatAuthenticationService.stream()
                .findFirst()
                .orElseThrow(() -> new SystemException("当前找不到微信任务服务，请设置 loncra.framework.wechat.enabled = true"))
                .getByPrincipal(principal);
    }

    @GetMapping("wechatInfo")
    @PreAuthorize("isAuthenticated()")
    @ConditionalOnProperty(prefix = "loncra.framework.wechat", value = "enabled", matchIfMissing = true)
    public WechatAuthenticationEntity getWechatAuthentication(@CurrentSecurityContext SecurityContext securityContext) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return wechatAuthenticationService.stream()
                .findFirst()
                .orElseThrow(() -> new SystemException("当前找不到微信任务服务，请设置 loncra.framework.wechat.enabled = true"))
                .getByPrincipal(token.getName());
    }

    @ResponseBody
    @PreAuthorize("isFullyAuthenticated()")
    @PostMapping("wechatAuthentication/sync")
    @ConditionalOnProperty(prefix = "loncra.framework.wechat", value = "enabled", matchIfMissing = true)
    public WechatUserDetails syncWechatAuthentication(
            @RequestParam
            String authenticationCode,
            @RequestParam
            String phoneNumberCode,
            HttpServletRequest request,
            HttpServletResponse response,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        return wechatAuthenticationService.stream()
                .findFirst()
                .orElseThrow(() -> new SystemException("当前找不到微信任务服务，请设置 loncra.framework.wechat.enabled = true"))
                .syncWechatAuthentication(authenticationCode, phoneNumberCode, request, response, securityContext);
    }

    /**
     * 同步插件資源
     *
     * @return reset 结果集
     */
    @Auditable
    @PostMapping("plugin/sync")
    @Plugin(name = "同步插件资源", parent = "authority_resource")
    @PreAuthorize("hasAuthority('perms[auth_server_authority_resource:sync_plugin_resource]')")
    public RestResult<Void> syncPluginResource() throws Exception {
        delegatingPluginResourceService.resubscribeAllService();
        return RestResult.of("同步数据完成");
    }
}
