package io.github.loncra.basic.service.resource.server.service.dictionary;

import com.alibaba.nacos.api.common.Constants;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import io.github.loncra.basic.service.commons.domain.metadata.TreeSortMetadata;
import io.github.loncra.basic.service.resource.api.domain.metadata.DataDictionaryMetadata;
import io.github.loncra.basic.service.resource.server.dao.dictionary.DataDictionaryDao;
import io.github.loncra.basic.service.resource.server.domain.entity.dictionary.DataDictionaryEntity;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.basic.DisabledOrEnabled;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * tb_data_dictionary 的业务逻辑
 *
 * <p>Table: tb_data_dictionary - 数据字典表</p>
 *
 * @author maurice.chen
 * @see DataDictionaryEntity
 * @since 2021-12-09 11:28:04
 */
@Service
@RequiredArgsConstructor
public class DataDictionaryService extends BasicService<DataDictionaryDao, DataDictionaryEntity> {

    /**
     * 获取数据字典
     *
     * @param code 代码
     *
     * @return 数据字典
     */
    public DataDictionaryEntity getByCode(String code) {
        return lambdaQuery().eq(DataDictionaryEntity::getCode, code)
                .one();
    }

    /**
     * 获取数据字典集合
     *
     * @param parentId 父类 id
     *
     * @return 数据字典集合
     */
    public List<DataDictionaryEntity> findByParentId(Long parentId) {
        return lambdaQuery().eq(DataDictionaryEntity::getParentId, parentId)
                .list();
    }

    /**
     * 获取数据字典集合
     *
     * @param typeId 字典类型 id
     *
     * @return 数据字典集合
     */
    public List<DataDictionaryEntity> findByTypeId(Long typeId) {
        return lambdaQuery().eq(DataDictionaryEntity::getTypeId, typeId)
                .list();
    }

    public List<DataDictionaryMetadata> findDataDictionaryMetas(String code) {

        return findByCode(code)
                .stream()
                .map(e -> CastUtils.of(e, DataDictionaryMetadata.class))
                .collect(Collectors.toList());
    }

    public List<DataDictionaryEntity> findByCode(String code) {
        int index = Strings.CS.indexOf(code, Constants.ALL_PATTERN);

        LambdaQueryChainWrapper<DataDictionaryEntity> wrapper = lambdaQuery().select(
                DataDictionaryEntity::getName,
                DataDictionaryEntity::getValue,
                DataDictionaryEntity::getId,
                DataDictionaryMetadata::getCode,
                DataDictionaryEntity::getParentId,
                DataDictionaryEntity::getEnabled,
                DataDictionaryMetadata::getMetadata,
                DataDictionaryEntity::getValueType,
                DataDictionaryEntity::getLevel
        );

        if (index > 0) {
            wrapper.likeRight(DataDictionaryEntity::getCode, StringUtils.substring(code, 0, index));
        }
        else {
            wrapper.eq(DataDictionaryEntity::getCode, code);
        }

        wrapper.eq(DataDictionaryEntity::getEnabled, DisabledOrEnabled.Enabled.getValue())
                .orderByAsc(DataDictionaryEntity::getSort);

        List<DataDictionaryEntity> result = wrapper.list();
        result.forEach(e -> e.setValue(CastUtils.cast(e.getValue(), e.getValueType()
                .getClassType())));

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void sort(List<TreeSortMetadata<Long>> sorts) {
        for (TreeSortMetadata<Long> sort : sorts) {
            lambdaUpdate().set(DataDictionaryEntity::getSort, sort.getSort())
                    .set(DataDictionaryEntity::getParentId, sort.getParentId())
                    .eq(DataDictionaryEntity::getId, sort.getId())
                    .update();
        }
    }
}
