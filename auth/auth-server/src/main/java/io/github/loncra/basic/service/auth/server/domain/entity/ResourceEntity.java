package io.github.loncra.basic.service.auth.server.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceCastegoryEnum;
import io.github.loncra.basic.service.auth.server.domain.metdata.ResourceMetadata;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.mybatis.plus.baisc.VersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Range;

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
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_resource", autoResultMap = true)
public class ResourceEntity extends ResourceMetadata implements VersionEntity<String, Long> {

    @Serial
    private static final long serialVersionUID = 4943319370503978271L;

    @ToString.Exclude
    private Long id;

    /**
     * 创建时间
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Instant creationTime;

    /**
     * 图标
     */
    @ToString.Exclude
    private String icon;

    /**
     * 前端页面路径
     */
    @ToString.Exclude
    private String page;

    /**
     * 类别
     */
    @ToString.Exclude
    private ResourceCastegoryEnum category;

    /**
     * 是否禁用
     */
    @ToString.Exclude
    private YesOrNo enabled;

    /**
     * 顺序值
     */
    @ToString.Exclude
    @Range(min = 0, max = 999)
    private Integer sort;

    /**
     * 备注
     */
    @ToString.Exclude
    private String remark;
}