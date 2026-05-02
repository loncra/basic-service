package io.github.loncra.basic.service.resource.server.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.commons.enumerate.DataRecordStatusEnum;
import io.github.loncra.basic.service.resource.api.enumerate.CarouselTypeEnum;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;


/**
 * <p>Table: tb_carousel - 轮播图表</p>
 *
 * @author maurice.chen
 * @since 2025-05-25 08:27:31
 */
@Data
@NoArgsConstructor
@Alias("carousel")
@TableName(value = "tb_carousel", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class CarouselEntity extends LongVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = 8722415009706345035L;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     */
    private CarouselTypeEnum type;

    /**
     * 链接
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private IdValueMetadata<String, String> link;

    /**
     * 状态
     */
    private DataRecordStatusEnum status = DataRecordStatusEnum.NEW;

    /**
     * 封面图片
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private ObjectWriteResult cover;

    /**
     * 备注
     */
    private String remark;

}