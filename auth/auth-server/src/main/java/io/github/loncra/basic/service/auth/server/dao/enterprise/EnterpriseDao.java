package io.github.loncra.basic.service.auth.server.dao.enterprise;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_enterprise 的数据访问
 *
 * <p>Table: tb_enterprise - 企业表</p>
 *
 * @author maurice.chen
 * @see EnterpriseEntity
 */
@Mapper
@Repository
public interface EnterpriseDao extends BaseMapper<EnterpriseEntity> {

}
