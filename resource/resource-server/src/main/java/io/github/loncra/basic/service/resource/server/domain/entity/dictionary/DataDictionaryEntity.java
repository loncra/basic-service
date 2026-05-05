package io.github.loncra.basic.service.resource.server.domain.entity.dictionary;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.github.loncra.basic.service.resource.api.domain.metadata.DataDictionaryMetadata;
import io.github.loncra.framework.commons.enumerate.basic.DisabledOrEnabled;
import io.github.loncra.framework.mybatis.plus.baisc.VersionEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;

/**
 * <p>数据字典实体类</p>
 * <p>Table: tb_data_dictionary - 数据字典</p>
 *
 * @author maurice
 * @since 2021-05-06 11:59:41
 */
@Data
@NoArgsConstructor
@Alias("dataDictionary")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_data_dictionary", autoResultMap = true)
public class DataDictionaryEntity extends DataDictionaryMetadata implements VersionEntity<Integer, Long> {

    @Serial
    private static final long serialVersionUID = 4219144269288469584L;

    private Long id;

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
    private Instant creationTime;

    /**
     * 版本号
     */
    @Version
    private Integer version;

    /**
     * 是否启用:0.禁用,1.启用
     */
    @NotNull
    private DisabledOrEnabled enabled;

    /**
     * 对应字典类型
     */
    private Long typeId;

    /**
     * 顺序值
     */
    private Integer sort = Integer.MAX_VALUE / 1000000;

    /**
     * 备注
     */
    private String remark;
}

