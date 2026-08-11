package io.github.loncra.basic.service.ai.api.domain.metadata.hub;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import io.github.loncra.basic.service.ai.api.domain.metadata.BasicPluginMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PackageOriginEnum;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PackageTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.DataStatusEnum;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.baisc.VersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PluginPackageMetadata extends BasicPluginMetadata implements VersionEntity<Integer, Long> {

    @Serial
    private static final long serialVersionUID = 5559699264457221003L;

    private Long id;

    private Instant creationTime;

    @Version
    private Integer version = 1;

    /**
     * 业务唯一键
     */
    private String packageKey;

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
     * schema 多态: transport/endpoint/toolGroup/oauth/ui 等
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> metadata = new LinkedHashMap<>();

    /**
     * 类型:10.系统,整体系统启动时自动加载，20.广场，供用户自定义安装
     */
    private PackageTypeEnum type;
}
