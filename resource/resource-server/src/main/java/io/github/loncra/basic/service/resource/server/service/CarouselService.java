package io.github.loncra.basic.service.resource.server.service;

import io.github.loncra.basic.service.commons.domain.metadata.FlatSortMetadata;
import io.github.loncra.basic.service.commons.enumerate.DataStatusEnum;
import io.github.loncra.basic.service.resource.server.dao.CarouselDao;
import io.github.loncra.basic.service.resource.server.domain.entity.CarouselEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * tb_carousel 的业务逻辑
 *
 * <p>Table: tb_carousel - 轮播图表</p>
 *
 * @author maurice.chen
 * @see CarouselEntity
 * @since 2025-05-25 08:27:31
 */
@Service
@RequiredArgsConstructor
public class CarouselService extends BasicService<CarouselDao, CarouselEntity> {

    @Transactional(rollbackFor = Exception.class)
    public void release(List<Long> ids) {
        ids.forEach(id -> lambdaUpdate().set(CarouselEntity::getStatus, DataStatusEnum.RELEASE.getValue())
                .eq(CarouselEntity::getId,id)
                .update()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void revoke(List<Long> ids) {

        ids.forEach(id -> lambdaUpdate().set(CarouselEntity::getStatus, DataStatusEnum.REVOKE.getValue())
                .eq(CarouselEntity::getId,id)
                .update()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void sort(List<FlatSortMetadata<Long>> sorts) {
        for (FlatSortMetadata<Long> sort : sorts) {
            lambdaUpdate().set(CarouselEntity::getSort, sort.getSort())
                    .eq(CarouselEntity::getId, sort.getId())
                    .update();
        }
    }

}
