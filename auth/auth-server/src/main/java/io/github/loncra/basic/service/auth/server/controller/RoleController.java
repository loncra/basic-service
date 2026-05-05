package io.github.loncra.basic.service.auth.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.domain.entity.RoleEntity;
import io.github.loncra.basic.service.auth.server.service.role.RoleService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.commons.tree.TreeUtils;
import io.github.loncra.framework.security.plugin.Plugin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

/**
 * 用户组管理
 *
 * @author maurice.chen
 * @see RoleEntity
 **/
@RestController
@RequestMapping("role")
@Plugin(
        name = "角色管理",
        id = "role",
        parent = "authority",
        authority = "perms[auth_server_role:find]",
        type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 获取所有用户组
     *
     * @param request         查询参数
     * @param mergeTree       是否合并树结构
     * @param idValueMetadata 是否返回 ID/值元数据
     *
     * @return REST 响应结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('perms[auth_server_role:find]')")
    public List<Object> find(
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "false")
            boolean mergeTree,
            @RequestParam(required = false, defaultValue = "false")
            boolean idValueMetadata
    ) {
        QueryWrapper<RoleEntity> query = roleService.getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        return findGroup(mergeTree, idValueMetadata, query);
    }

    private List<Object> findGroup(
            @RequestParam(required = false, defaultValue = "false")
            boolean mergeTree,
            @RequestParam(required = false, defaultValue = "false")
            boolean idValueMetadata,
            QueryWrapper<RoleEntity> query
    ) {
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        List<RoleEntity> roleList = roleService.find(query);

        if (mergeTree) {
            return new LinkedList<>(TreeUtils.buildGenericTree(roleList));
        }
        else if (idValueMetadata) {
            List<Object> result = new LinkedList<>();
            roleList.stream()
                    .map(g -> TypeIdNameMetadata.of(g.getId().toString(), g.getName(), g.getAuthority()))
                    .forEach(result::add);
            return result;
        }
        else {
            return new LinkedList<>(roleList);
        }
    }

    /**
     * 获取用户组
     *
     * @param id 主键值
     *
     * @return REST 响应结果
     */
    @GetMapping("{id:\\d+}")
    @Plugin(name = "查看明细")
    @PreAuthorize("hasAuthority('perms[auth_server_role:get]')")
    public RoleEntity get(
            @PathVariable
            Long id
    ) {
        return roleService.get(id);
    }

    /**
     * 保存用户组
     *
     * @param entity 用户组实体
     *
     * @return REST 响应结果
     */
    @PutMapping
    @PreAuthorize("hasAuthority('perms[auth_server_role:save]')")
    @Plugin(name = "添加或保存信息", operationDataTrace = true)
    public RestResult<Long> save(
            @Valid
            @RequestBody
            RoleEntity entity
    ) {
        roleService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除用户组
     *
     * @param ids 主键值集合
     *
     * @return REST 响应结果
     */
    @DeleteMapping
    @PreAuthorize("hasAuthority('perms[auth_server_role:delete]')")
    @Plugin(name = "删除信息", operationDataTrace = true)
    public RestResult<Void> delete(
            @RequestParam
            List<Long> ids
    ) {
        roleService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 判断 spring security role 的 authority 值是否唯一
     *
     * @param authority spring security role 的 authority 值
     *
     * @return REST 响应结果
     */
    @GetMapping("unique/authority/{authority}")
    @PreAuthorize("isAuthenticated()")
    public Boolean isAuthorityUnique(
            @PathVariable
            String authority
    ) {
        return !roleService.lambdaQuery()
                .eq(RoleEntity::getAuthority, authority)
                .exists();
    }

    /**
     * 判断组名称是否唯一
     *
     * @param name 组名称
     *
     * @return REST 响应结果
     */
    @GetMapping("unique/name/{name}")
    @PreAuthorize("isAuthenticated()")
    public Boolean isNameUnique(
            @PathVariable
            String name
    ) {
        return !roleService.lambdaQuery()
                .eq(RoleEntity::getName, name)
                .exists();
    }
}
