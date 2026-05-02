package io.github.loncra.basic.service.resource.server.service.dictionary;

import io.github.loncra.basic.service.resource.server.dao.dictionary.DictionaryTypeDao;
import io.github.loncra.basic.service.resource.server.domain.entity.dictionary.DictionaryTypeEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * tb_dictionary_type 的业务逻辑
 *
 * <p>Table: tb_dictionary_type - 数据字典类型表</p>
 *
 * @author maurice.chen
 * @see DictionaryTypeEntity
 * @since 2021-12-09 11:28:04
 */
@Service
@RequiredArgsConstructor
public class DictionaryTypeService extends BasicService<DictionaryTypeDao, DictionaryTypeEntity> {

    /**
     * 获取数据字典
     *
     * @param code 代码
     *
     * @return 数据字典
     */
    public DictionaryTypeEntity getByCode(String code) {
        return lambdaQuery().eq(DictionaryTypeEntity::getCode, code)
                .one();
    }

    /**
     * 获取数据字典集合
     *
     * @param parentId 父类 id
     *
     * @return 数据字典集合
     */
    public List<DictionaryTypeEntity> getByParentId(Long parentId) {
        return lambdaQuery().eq(DictionaryTypeEntity::getParentId, parentId)
                .list();
    }
}
