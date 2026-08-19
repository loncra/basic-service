package io.github.loncra.basic.service.ai.server.controller.hub;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillPackageEntity;
import io.github.loncra.basic.service.ai.server.service.hub.AiSkillPackageService;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 *
 * tb_ai_skill_package 的控制器
 *
 * @see AiSkillPackageEntity
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@RestController
@RequestMapping("ai/skill/package")
@Plugin(
    name = "技能广场配置",
    id = "ai_skill_package",
    parent = "config",
    authority = "perms[ai_skill_package:page]",
    type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
    sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class AiSkillPackageController {

    private final AiSkillPackageService aiSkillPackageService;

    /**
     * 获取分页
     *
     * @param pageRequest 分页信息
     * @param request  http servlet request
     *
     * @return 分页实体
     *
     * @see AiSkillPackageEntity
     */
    @PostMapping
    @PreAuthorize("hasAuthority('perms[ai_skill_package:page]')")
    public Page<AiSkillPackageEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<AiSkillPackageEntity> query = aiSkillPackageService
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return aiSkillPackageService.findPage(pageRequest, query);
    }

    /**
     * 获取明细
     *
     * @param id 主键 ID
     *
     * @return REST 响应结果
     *
     * @see AiSkillPackageEntity
     */
    @Plugin(name = "查看明细")
    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('perms[ai_skill_package:get]')")
    public AiSkillPackageEntity get(@PathVariable Long id) {
        return aiSkillPackageService.get(id);
    }

    /**
     * 保存数据
     *
     * @param entity 数据请求体
     *
     * @see AiSkillPackageEntity
     */
    @PutMapping
    @OperationDataTrace
    @Plugin(name = "保存或添加信息")
    @PreAuthorize("hasAuthority('perms[ai_skill_package:save]')")
    public RestResult<Long> save(@Valid @RequestBody AiSkillPackageEntity entity) {
        aiSkillPackageService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除数据
     *
     * @param ids 主键 ID 值集合
     *
     * @see AiSkillPackageEntity
     */
    @DeleteMapping
    @OperationDataTrace
    @Plugin(name = "删除信息")
    @PreAuthorize("hasAuthority('perms[ai_skill_package:delete]')")
    public RestResult<Void> delete(@RequestParam List<Long> ids) {
        aiSkillPackageService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }
    /**
     * 发布数据
     *
     * @param ids 主键 ID 集合
     *
     * @return REST 响应结果
     */
    @OperationDataTrace
    @Plugin(name = "发布信息")
    @PostMapping("release")
    @PreAuthorize("hasAuthority('perms[ai_skill_package:release]')")
    public RestResult<Void> release(
            @RequestParam
            List<Long> ids
    ) {
        aiSkillPackageService.release(ids);
        return RestResult.of("发布 " + ids.size() + " 条记录成功");
    }

    /**
     * 下架数据
     *
     * @param ids 主键 ID 集合
     *
     * @return REST 响应结果
     */
    @OperationDataTrace
    @Plugin(name = "下架信息")
    @PostMapping("revoke")
    @PreAuthorize("hasAuthority('perms[ai_skill_package:revoke]')")
    public RestResult<Void> revoke(
            @RequestParam
            List<Long> ids
    ) {
        aiSkillPackageService.revoke(ids);
        return RestResult.of("下架 " + ids.size() + " 条记录成功");
    }
}
