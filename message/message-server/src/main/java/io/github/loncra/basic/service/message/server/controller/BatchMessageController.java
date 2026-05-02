package io.github.loncra.basic.service.message.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.message.server.domain.entity.BatchMessageEntity;
import io.github.loncra.basic.service.message.server.service.BatchMessageService;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.plugin.Plugin;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 批量消息管理
 *
 * @author maurice
 * @see BatchMessageEntity
 * @since 2021-08-22 04:45:14
 */
@RestController
@RequestMapping("batch")
@Plugin(
        name = "批量消息",
        id = "batch",
        parent = "message",
        authority = "perms[message_server_batch_message:page]",
        type = SystemConstants.RESOURCE_MENU_TYPE,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class BatchMessageController {

    private final BatchMessageService batchMessageService;

    /**
     * 获取分页
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     *
     * @return REST 响应结果
     *
     * @see BatchMessageEntity
     */
    @PostMapping
    @PreAuthorize("hasAuthority('perms[message_server_batch_message:page]')")
    public Page<BatchMessageEntity> page(
            PageRequest pageRequest,
            HttpServletRequest request
    ) {
        QueryWrapper<BatchMessageEntity> query = batchMessageService
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return batchMessageService.findTotalPage(pageRequest, query);
    }

    /**
     * 获取明细
     *
     * @param id 主键 ID
     *
     * @return REST 响应结果
     *
     * @see BatchMessageEntity
     */
    @GetMapping("/{id:\\d+}")
    @Plugin(name = "查看明细")
    @PreAuthorize("hasAuthority('perms[message_server_batch_message:get]')")
    public BatchMessageEntity get(
            @RequestParam("id")
            Long id
    ) {
        return batchMessageService.get(id);
    }

    /**
     * 删除数据
     *
     * @param ids 主键 ID 集合
     *
     * @see BatchMessageEntity
     */
    @DeleteMapping
    @Plugin(name = "删除信息", operationDataTrace = true)
    @PreAuthorize("hasAuthority('perms[message_server_batch_message:delete]')")
    public RestResult<Void> delete(
            @RequestParam
            List<Long> ids
    ) {
        batchMessageService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
