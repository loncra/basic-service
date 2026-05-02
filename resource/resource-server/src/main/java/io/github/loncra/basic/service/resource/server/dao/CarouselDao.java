package io.github.loncra.basic.service.resource.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.resource.server.domain.entity.CarouselEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_carousel 的数据访问
 *
 * <p>Table: tb_carousel - 轮播图表</p>
 *
 * @author maurice.chen
 * @see CarouselEntity
 * @since 2025-05-25 08:27:31
 */
@Mapper
@Repository
public interface CarouselDao extends BaseMapper<CarouselEntity> {

}
