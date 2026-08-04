package io.github.loncra.basic.service.ai.server.domain.metadata.hub;

import com.baomidou.mybatisplus.annotation.TableField;
import io.github.loncra.basic.service.ai.server.enumerate.hub.PackageOriginEnum;
import io.github.loncra.basic.service.ai.server.enumerate.hub.PackageTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.DataStatusEnum;
import io.github.loncra.basic.service.resource.api.domain.metadata.DataDictionaryMetadata;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PluginPackageMetadata extends LongVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = 5559699264457221003L;

    /**
     * 业务唯一键
     */
    private String packageKey;

    /**
     * 展示名称
     */
    private String name;

    /**
     * 列表摘要
     */
    private String summary;

    /**
     * 状态:10.内部，20.外部
     */
    private PackageOriginEnum origin;

    /**
     * 状态:10.新创建, 20.已发布, 30.已下架
     */
    private DataStatusEnum status;

    /**
     * 标签
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<String> tags = new LinkedList<>();

    /**
     * schema 多态: transport/endpoint/toolGroup/oauth/ui 等
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> metadata = new LinkedHashMap<>();

    /**
     * 类型:10.系统,整体系统启动时自动加载，20.广场，供用户自定义安装
     */
    private PackageTypeEnum type;

    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private DataDictionaryMetadata group;
}
