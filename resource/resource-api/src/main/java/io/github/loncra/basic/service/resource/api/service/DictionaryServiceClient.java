package io.github.loncra.basic.service.resource.api.service;

import io.github.loncra.basic.service.resource.api.domain.metadata.DataDictionaryMetadata;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 字典服务客户端
 *
 * @author maurice.chen
 */
public interface DictionaryServiceClient {

    /**
     * 根据数名称获取数据字典集合
     *
     * @param name 字典名称
     *
     * @return 数据字典集合
     */
    List<DataDictionaryMetadata> findDataDictionaries(
            @PathVariable("code")
            String name
    );

    /**
     * 根据字典类型查询数据字典
     *
     * @param typeId 字典类型 id
     *
     * @return 数据字典集合
     */
    List<DataDictionaryMetadata> findDataDictionariesByTypeId(
            @RequestParam("typeId")
            Long typeId
    );

    /**
     * 根据数据字典代码获取数据字典
     *
     * @param code 数据字典代码
     *
     * @return 数据字典集合
     */
    DataDictionaryMetadata getDataDictionaryByCode(
            @RequestParam("code")
            String code
    );

    /**
     * 通过名称获取数据字典
     *
     * @param typeCode 字典类型代码
     * @param name     字典名称
     *
     * @return 资源数据字典元数据信息
     */
    DataDictionaryMetadata getDataDictionaryByName(
            String typeCode,
            String name
    );

    /**
     * 获取资源字典
     *
     * @param code 字典代码
     *
     * @return 资源字典
     */
    DataDictionaryMetadata getResourceDictionary(
            String code
    );
}
