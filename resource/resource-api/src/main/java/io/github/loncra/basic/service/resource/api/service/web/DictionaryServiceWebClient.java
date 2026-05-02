package io.github.loncra.basic.service.resource.api.service.web;

import io.github.loncra.basic.service.resource.api.domain.metadata.DataDictionaryMetadata;
import io.github.loncra.basic.service.resource.api.service.DictionaryServiceClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

/**
 * 字典服务客户端
 *
 * @author maurice.chen
 */
@HttpExchange
public interface DictionaryServiceWebClient extends DictionaryServiceClient {

    /**
     * 根据数名称获取数据字典集合
     *
     * @param name 字典名称
     *
     * @return 数据字典集合
     */
    @Override
    @GetExchange("findDataDictionaries/{code}")
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
    @Override
    @GetExchange("findDataDictionariesByTypeId")
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
    @Override
    @GetExchange("getDataDictionaryByCode")
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
    @Override
    @GetExchange("getDataDictionaryByName")
    DataDictionaryMetadata getDataDictionaryByName(
            @RequestParam("typeCode")
            String typeCode,
            @RequestParam("name")
            String name
    );

    /**
     * 获取资源字典
     *
     * @param code 字典代码
     *
     * @return 资源字典
     */
    @Override
    @GetExchange("getDataDictionary/{code}")
    DataDictionaryMetadata getResourceDictionary(
            @PathVariable
            String code
    );
}
