package io.github.loncra.basic.service.resource.server.dao.dictionary;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.resource.server.domain.entity.dictionary.DictionaryTypeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_dictionary_type 字典类型数据访问
 *
 * <p>Table: tb_dictionary_type - 字典类型</p>
 *
 * @author maurice
 * @see DictionaryTypeEntity
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface DictionaryTypeDao extends BaseMapper<DictionaryTypeEntity> {
}

