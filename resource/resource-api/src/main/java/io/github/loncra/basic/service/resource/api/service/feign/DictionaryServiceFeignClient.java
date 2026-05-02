package io.github.loncra.basic.service.resource.api.service.feign;

import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.resource.api.domain.metadata.DataDictionaryMetadata;
import io.github.loncra.basic.service.resource.api.service.DictionaryServiceClient;
import io.github.loncra.framework.spring.security.core.authentication.service.feign.FeignAuthenticationConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 字典服务客户端
 *
 * @author maurice.chen
 */
@ConditionalOnClass(FeignClientsConfiguration.class)
@FeignClient(name = SystemConstants.SYS_RESOURCE_NAME, contextId = "dictionaryServiceClient", configuration = FeignAuthenticationConfiguration.class)
public interface DictionaryServiceFeignClient extends DictionaryServiceClient {

    /**
     * 根据数名称获取数据字典集合
     *
     * @param name 字典名称
     *
     * @return 数据字典集合
     */
    @Override
    @GetMapping("findDataDictionaries/{code}")
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
    @GetMapping("findDataDictionariesByTypeId")
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
    @GetMapping("getDataDictionaryByCode")
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
    @GetMapping("getDataDictionaryByName")
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
    @GetMapping("getDataDictionary/{code}")
    DataDictionaryMetadata getResourceDictionary(
            @PathVariable
            String code
    );
}
