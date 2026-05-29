package io.github.loncra.basic.service.auth.server.controller;

import io.github.loncra.basic.service.auth.server.domain.entity.merchant.OpenPlatformMerchantClientEntity;
import io.github.loncra.basic.service.auth.server.service.merchant.OpenPlatformMerchantClientService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 开放平台商户客户端管理
 *
 * @author maurice.chen
 * @see OpenPlatformMerchantClientEntity
 * @since 2023-11-23 08:46:39
 */
@RestController
@RequestMapping("open/platform/merchant/client")
@Plugin(
        name = "开放平台商户客户端管理",
        id = "open_platform_merchant_client",
        authority = "perms[auth_server_open_platform_merchant_client:get_by_merchant_id]",
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class OpenPlatformMerchantClientController {

    private final OpenPlatformMerchantClientService openPlatformMerchantClientService;

    /**
     * 按商户 ID 获取开放平台商户客户端集合
     *
     * @param id 主键 ID
     *
     * @return REST 响应结果
     *
     * @see OpenPlatformMerchantClientEntity
     */
    @GetMapping("byMerchantId/{id:\\d+}")
    @PreAuthorize("hasAuthority('perms[auth_server_open_platform_merchant_client:by_merchant_id]')")
    public List<OpenPlatformMerchantClientEntity> getByMerchantId(
            @PathVariable
            Long id
    ) {
        return openPlatformMerchantClientService.getByMerchantId(id);
    }

    /**
     * 保存数据
     *
     * @param entity 数据请求体
     *
     * @see OpenPlatformMerchantClientEntity
     */
    @PutMapping
    @OperationDataTrace
    @Plugin(name = "保存或添加信息")
    @PreAuthorize("hasAuthority('perms[auth_server_open_platform_merchant_client:save]')")
    public RestResult<String> save(
            @Valid
            @RequestBody
            OpenPlatformMerchantClientEntity entity
    ) {
        openPlatformMerchantClientService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 获取明细
     *
     * @param id 主键值
     *
     * @return REST 响应结果
     */
    @GetMapping("{id:\\d+}")
    @PreAuthorize("hasAuthority('perms[auth_server_open_platform_merchant_client:get]')")
    @Plugin(name = "查看明细")
    public OpenPlatformMerchantClientEntity get(
            @PathVariable
            Long id
    ) {
        return openPlatformMerchantClientService.get(id);
    }
}
