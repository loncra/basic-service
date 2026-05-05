package io.github.loncra.basic.service.auth.server.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceCastegoryEnum;
import io.github.loncra.basic.service.auth.server.domain.metdata.ResourceMetadata;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.mybatis.plus.baisc.VersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;


/**
 * <p>Table: tb_resource - 资源表</p>
 *
 * @author maurice.chen
 *
 * @since 2026-05-04 09:51:55
 */
@Data
@NoArgsConstructor
@Alias("resource")
@TableName("tb_resource")
@EqualsAndHashCode(callSuper = true)
public class ResourceEntity extends ResourceMetadata implements VersionEntity<String, Long> {

    @Serial
    private static final long serialVersionUID = 4943319370503978271L;

    private Long id;

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
    private Instant creationTime;

    /**
     * 图标
     */
    private String icon;

    /**
     * 前端页面路径
     */
    private String page;

    /**
     * 备注
     */
    private String remark;

    /**
     * 类别
     */
    private ResourceCastegoryEnum category;

    /**
     * 是否禁用
     */
    private YesOrNo enabled;
}