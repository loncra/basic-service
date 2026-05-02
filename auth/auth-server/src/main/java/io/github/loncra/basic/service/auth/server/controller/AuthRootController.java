package io.github.loncra.basic.service.auth.server.controller;

import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.server.domain.entity.WechatAuthenticationEntity;
import io.github.loncra.basic.service.auth.server.enumerate.oauth.RegisteredClientScopeEnum;
import io.github.loncra.basic.service.auth.server.security.handler.JsonLogoutSuccessHandler;
import io.github.loncra.basic.service.auth.server.service.RedissonCacheAuthorizationService;
import io.github.loncra.basic.service.auth.server.service.WechatAuthenticationService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.HttpRequestParameterMapUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdNameMetadata;
import io.github.loncra.framework.commons.id.metadata.IdNameValueMetadata;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.audit.IdAuditEvent;
import io.github.loncra.framework.security.plugin.Plugin;
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

    private final RedissonCacheAuthorizationService<AbstractBasicSystemUser> redissonCacheAuthorizationService;

    private final List<JsonAuthenticationSuccessResponse> successResponses;

    private final ObjectProvider<WechatAuthenticationService> wechatAuthenticationService;

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
    @PutMapping("user/password/update")
    @PreAuthorize("isFullyAuthenticated()")
    @Plugin(name = "修改个人登录密码", operationDataTrace = true, parent = "authority")
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
        redissonCacheAuthorizationService.updatePassword(token, oldPassword, newPassword);
        return RestResult.of("修改密码成功");
    }

    /**
     * 更新系统用户登录密码
     *
     * @param type 用户类型
     * @param id   用户 id
     */
    @ResponseBody
    @PutMapping("user/password/admin/reset")
    @PreAuthorize("hasAuthority('auth_server_system_user:admin_reset_password')")
    @Plugin(name = "重置用户密码", operationDataTrace = true, parent = "authority")
    public RestResult<Object> adminRestPassword(
            String type,
            String id
    ) {
        String newPassword = redissonCacheAuthorizationService.adminRestPassword(type, id);
        return RestResult.ofSuccess("重置密码成功", (Object) newPassword);
    }

    /**
     * 获取系统用户
     *
     * @param metadata 带名称的 id 元数据
     *
     * @return REST 响应结果
     */
    @ResponseBody
    @PostMapping("systemUser")
    @PreAuthorize("hasRole('FEIGN')")
    public AbstractBasicSystemUser systemUser(
            @RequestBody
            TypeIdNameMetadata metadata
    ) {
        return redissonCacheAuthorizationService.getSystemUserAuthorizationResolver(metadata.getType(), true)
                .getByIdentity(metadata.getId());
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
    @PostMapping("createByPhoneNumber")
    @PreAuthorize("hasRole('FEIGN')")
    public AbstractBasicSystemUser createSystemUserByPhoneNumber(
            @RequestParam
            String phoneNumber,
            @RequestParam
            String type
    ) {
        return redissonCacheAuthorizationService.getSystemUserAuthorizationResolver(type, true)
                .createByPhoneNumber(phoneNumber);
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
    @PostMapping("systemUsers")
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
        Map<IdNameMetadata, List<AbstractBasicSystemUser>> result = redissonCacheAuthorizationService.findSystemUser(pageRequest, ignoreTypes, request);
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
    @GetMapping("systemUsers/{type}")
    public List<AbstractBasicSystemUser> findSystemUser(
            @PathVariable(required = false) String type,
            HttpServletRequest request
    ) {
        MultiValueMap<String, Object> filter = new LinkedMultiValueMap<>();
        HttpRequestParameterMapUtils.castMapToMultiValueMap(request.getParameterMap())
                .forEach(filter::add);
        if (StringUtils.isEmpty(type)) {
            return redissonCacheAuthorizationService.getSystemUserAuthorizationResolvers()
                    .stream()
                    .flatMap(s -> s.findPage(PageRequest.of(-1), filter).getElements().stream())
                    .toList();
        }
        else {
            return redissonCacheAuthorizationService.getSystemUserAuthorizationResolver(type, true)
                    .findPage(PageRequest.of(-1), filter)
                    .getElements();
        }
    }

    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    @GetMapping("systemUser/{type}/{id}")
    public AbstractBasicSystemUser getSystemUser(
            @PathVariable String type,
            @PathVariable Long id
    ) {

        return redissonCacheAuthorizationService.getSystemUserAuthorizationResolver(type, true)
                .getByIdentity(id.toString());
    }

    @ResponseBody
    @GetMapping("systemUserTypes")
    @PreAuthorize("isAuthenticated()")
    public List<IdNameMetadata> getSystemUserTypes() {
        return redissonCacheAuthorizationService.getSystemUserTypes();
    }

    @ResponseBody
    @PreAuthorize("hasRole('FEIGN')")
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
}
