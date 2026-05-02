package io.github.loncra.basic.service.resource.server.service.dictionary;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.loncra.basic.service.resource.api.domain.metadata.DataDictionaryMetadata;
import io.github.loncra.basic.service.resource.server.config.ResourceAppConfig;
import io.github.loncra.basic.service.resource.server.domain.entity.dictionary.DataDictionaryEntity;
import io.github.loncra.basic.service.resource.server.domain.entity.dictionary.DictionaryTypeEntity;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.ServiceException;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.tree.TreeUtils;
import io.github.loncra.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
import io.github.loncra.framework.idempotent.annotation.Concurrent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Strings;
import org.redisson.api.RList;
import org.redisson.api.options.KeysOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 字典管理
 *
 * @author maurice
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryService {

    public final static String CONCURRENT_PREFIX = "loncra:basic-service:resource:app:data-dictionary:concurrent:";

    @Getter
    private final DataDictionaryService dataDictionaryService;

    @Getter
    private final DictionaryTypeService dictionaryTypeService;

    private final ResourceAppConfig resourceApplicationConfig;

    private final ConcurrentInterceptor concurrentInterceptor;

    @Concurrent(CONCURRENT_PREFIX + "[#typeId]")
    public List<DataDictionaryMetadata> findDataDictionaries(Long typeId) {

        RList<DataDictionaryMetadata> data = concurrentInterceptor.getRedissonClient()
                .getList(resourceApplicationConfig.getDictionaryCache().getName(typeId));

        if (CollectionUtils.isNotEmpty(data)) {
            return data;
        }

        List<DataDictionaryMetadata> metas = getDataDictionaryService().lambdaQuery()
                .eq(DataDictionaryEntity::getTypeId, typeId)
                .list()
                .stream()
                .map(d -> CastUtils.of(d, DataDictionaryMetadata.class))
                .collect(Collectors.toList());
        data.clear();
        data.addAllAsync(TreeUtils.buildGenericTree(metas));

        return data;
    }

    // ----------------------------------------- 数据字典管理 ----------------------------------------- //


    /**
     * 保存数据字典
     *
     * @param entity 数据字典实体
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDataDictionary(DataDictionaryEntity entity) {
        if (Objects.nonNull(entity.getId())) {
            updateDataDictionary(entity, true);
        }
        else {
            insertDataDictionary(entity);
        }

        concurrentInterceptor
                .getRedissonClient()
                .getKeys(KeysOptions.defaults())
                .delete(resourceApplicationConfig.getDictionaryCache().getName(entity.getTypeId()));
    }

    /**
     * 更新数据字典
     *
     * @param entity        数据字典实体
     * @param updateKeyPath 是否更新父类键路径, true 是, 否则 false
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDataDictionary(
            DataDictionaryEntity entity,
            boolean updateKeyPath
    ) {

        setDataDictionaryParentCode(entity, updateKeyPath);

        DataDictionaryEntity dataDictionary = dataDictionaryService.get(entity.getId());

        if (!entity.getCode().equals(dataDictionary.getCode())) {

            DataDictionaryEntity exist = dataDictionaryService.getByCode(entity.getCode());

            if (Objects.nonNull(exist)) {
                String msg = entity.getCode() + "键已被数据字典[" + exist.getName() + "]使用，无法更改";
                throw new ServiceException(msg);
            }

            List<DataDictionaryEntity> dataDictionaries = dataDictionaryService.findByParentId(dataDictionary.getId());

            for (DataDictionaryEntity dd : dataDictionaries) {
                String newKey = Strings.CS.replace(dd.getCode(), dataDictionary.getCode(), entity.getCode());
                dd.setCode(newKey);
                updateDataDictionary(dd, false);
            }
        }

        dataDictionaryService.updateById(entity);
    }

    /**
     * 新增数据字典
     *
     * @param entity 数据字典实体
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertDataDictionary(DataDictionaryEntity entity) {

        DictionaryTypeEntity type = dictionaryTypeService.get(entity.getTypeId());

        if (Objects.isNull(type)) {
            throw new ServiceException("找不到 ID 为 [" + entity.getTypeId() + "] 的字典类型");
        }

        setDataDictionaryParentCode(entity, true);

        DataDictionaryEntity dataDictionary = dataDictionaryService.getByCode(entity.getCode());

        if (Objects.nonNull(dataDictionary)) {
            throw new ServiceException("键为 [" + entity.getCode() + "] 已存在");
        }

        dataDictionaryService.insert(entity);
    }

    /**
     * 设置数据字典父类键路径
     *
     * @param entity        数据字典实体
     * @param updateKeyPath 是否更新父类键路径, true 是, 否则 false
     */
    private void setDataDictionaryParentCode(
            DataDictionaryEntity entity,
            boolean updateKeyPath
    ) {

        if (!updateKeyPath) {
            return;
        }

        if (Objects.nonNull(entity.getParentId())) {

            DataDictionaryEntity parent = Objects.requireNonNull(
                    dataDictionaryService.get(entity.getParentId()),
                    "找不到ID为 [" + entity.getParentId() + "] 的父类信息"
            );

            if (!entity.getCode().startsWith(parent.getCode() + resourceApplicationConfig.getDictionarySeparator())) {
                entity.setCode(parent.getCode() + resourceApplicationConfig.getDictionarySeparator() + entity.getCode());
            }

        }
        else {
            DictionaryTypeEntity dictionaryType = dictionaryTypeService.get(entity.getTypeId());
            if (!entity.getCode().startsWith(dictionaryType.getCode() + resourceApplicationConfig.getDictionarySeparator())) {
                entity.setCode(dictionaryType.getCode() + resourceApplicationConfig.getDictionarySeparator() + entity.getCode());
            }
        }
    }

    /**
     * 删除数据字典
     *
     * @param ids 主键 id 集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDataDictionary(List<Long> ids) {

        Wrapper<DataDictionaryEntity> parentWrapper = Wrappers
                .<DataDictionaryEntity>lambdaQuery()
                .select(DataDictionaryEntity::getId)
                .in(DataDictionaryEntity::getParentId, ids);

        List<Long> subIds = dataDictionaryService.findObjects(parentWrapper, Long.class);

        if (CollectionUtils.isNotEmpty(subIds)) {
            deleteDataDictionary(subIds);
        }

        Wrapper<DataDictionaryEntity> typeWrapper = Wrappers
                .<DataDictionaryEntity>lambdaQuery()
                .select(DataDictionaryEntity::getTypeId)
                .in(DataDictionaryEntity::getId, ids);

        List<Long> typeId = dataDictionaryService.findObjects(typeWrapper, Long.class);

        concurrentInterceptor
                .getRedissonClient()
                .getKeys(KeysOptions.defaults())
                .delete(typeId.stream().map(k -> resourceApplicationConfig.getDictionaryCache().getName(k)).toArray(String[]::new));

        dataDictionaryService.deleteById(ids);

    }

    // ----------------------------------------- 字典类型管理 ----------------------------------------- //

    /**
     * 保存字典类型实体
     *
     * @param entity 字典类型实体
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDictionaryType(DictionaryTypeEntity entity) {
        if (Objects.nonNull(entity.getId())) {
            updateDictionaryType(entity, true);
        }
        else {
            insertDictionaryType(entity);
        }
    }

    /**
     * 新增字典类型
     *
     * @param entity 字典类型实体
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertDictionaryType(DictionaryTypeEntity entity) {

        setDictionaryTypeParentCode(entity, true);

        if (Objects.nonNull(dictionaryTypeService.getByCode(entity.getCode()))) {
            throw new ServiceException("键为 [" + entity.getCode() + "] 已存在");
        }

        dictionaryTypeService.insert(entity);
    }

    /**
     * 设置数据类型父类键路径
     *
     * @param entity        数据类型实体
     * @param updateKeyPath 是否更新父类键路径, true 是, 否则 false
     */
    private void setDictionaryTypeParentCode(
            DictionaryTypeEntity entity,
            boolean updateKeyPath
    ) {

        if (!updateKeyPath) {
            return;
        }

        if (Objects.nonNull(entity.getParentId())) {

            DictionaryTypeEntity parent = Objects.requireNonNull(
                    dictionaryTypeService.get(entity.getParentId()),
                    "找不到ID为 [" + entity.getParentId() + "] 的父类信息"
            );

            if (!entity.getCode().startsWith(parent.getCode() + resourceApplicationConfig.getDictionarySeparator())) {
                entity.setCode(parent.getCode() + resourceApplicationConfig.getDictionarySeparator() + entity.getCode());
            }

        }
    }

    /**
     * 更新字典类型
     *
     * @param entity        字典类型实体
     * @param updateKeyPath 是否更新父类键路径, true 是, 否则 false
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDictionaryType(
            DictionaryTypeEntity entity,
            boolean updateKeyPath
    ) {

        setDictionaryTypeParentCode(entity, updateKeyPath);

        DictionaryTypeEntity dictionaryType = dictionaryTypeService.get(entity.getId());

        if (!dictionaryType.getCode().equals(entity.getCode())) {

            DictionaryTypeEntity exist = dictionaryTypeService.getByCode(entity.getCode());

            if (Objects.nonNull(exist)) {
                String msg = entity.getCode() + "键已被字典类型[" + exist.getName() + "]使用，无法更改";
                throw new ServiceException(msg);
            }

            List<DictionaryTypeEntity> dictionaryTypes = dictionaryTypeService.getByParentId(dictionaryType.getId());

            for (DictionaryTypeEntity dt : dictionaryTypes) {
                String newKey = Strings.CS.replace(dt.getCode(), dictionaryType.getCode(), entity.getCode());
                dt.setCode(newKey);
                updateDictionaryType(dt, false);
            }

            List<DataDictionaryEntity> dataDictionaries = dataDictionaryService.findByTypeId(entity.getId());

            for (DataDictionaryEntity dataDictionary : dataDictionaries) {
                String newKey = Strings.CS.replace(dataDictionary.getCode(), dictionaryType.getCode(), entity.getCode());
                dataDictionary.setCode(newKey);
                updateDataDictionary(dataDictionary, false);
            }
        }

        dictionaryTypeService.updateById(entity);
    }

    /**
     * 删除字典类型
     *
     * @param ids 主键 id 集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictionaryType(List<Long> ids) {
        Wrapper<DictionaryTypeEntity> wrapper = Wrappers.<DictionaryTypeEntity>lambdaQuery()
                .select(DictionaryTypeEntity::getId)
                .in(DictionaryTypeEntity::getParentId, ids);

        List<Long> subIds = dictionaryTypeService.findObjects(wrapper, Long.class);

        if (CollectionUtils.isNotEmpty(subIds)) {
            subIds.stream()
                    .flatMap(id -> dataDictionaryService.findByTypeId(id).stream())
                    .forEach(dataDictionaryService::deleteByEntity);
        }

        if (CollectionUtils.isNotEmpty(subIds)) {
            deleteDictionaryType(subIds);
        }

        dictionaryTypeService.deleteById(ids);
    }

    public Map<Long, List<DataDictionaryMetadata>> findGroupDataDictionariesByTypeIds(List<Long> typeIds) {
        Map<Long, List<DataDictionaryMetadata>> group = new LinkedHashMap<>();

        for (Long typeId : typeIds) {
            group.put(typeId, concurrentInterceptor.invoke(CONCURRENT_PREFIX + typeId, () -> findDataDictionaries(typeId)));
        }

        return group;
    }


    public Map<String, List<DataDictionaryMetadata>> findGroupDataDictionariesByCodes(List<String> codes) {
        Map<String, List<DataDictionaryMetadata>> group = new LinkedHashMap<>();

        for (String code : codes) {
            List<DataDictionaryMetadata> result = dataDictionaryService.findDataDictionaryMetas(code);
            TreeUtils.buildGenericTree(result);
            group.put(code, result);
        }

        return group;
    }

    public DataDictionaryEntity getDataDictionaryByName(
            String typeCode,
            String name
    ) {
        DictionaryTypeEntity type = Objects.requireNonNull(getDictionaryTypeService().getByCode(typeCode), "找不到代码为 [" + typeCode + "] 的字典类型数据");
        List<DataDictionaryEntity> dataDictionaries = getDataDictionaryService().findByTypeId(type.getId());
        return dataDictionaries
                .stream()
                .filter(s -> Strings.CS.equals(s.getName(), name))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到名称为 [" + name + "] 的数据字典"));
    }
}
