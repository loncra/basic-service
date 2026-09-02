package io.github.loncra.basic.service.auth.server.dao.enterprise;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseMemberEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_enterprise_member 的数据访问
 *
 * <p>Table: tb_enterprise_member - 企业成员表</p>
 *
 * @author maurice.chen
 * @see EnterpriseMemberEntity
 */
@Mapper
@Repository
public interface EnterpriseMemberDao extends BaseMapper<EnterpriseMemberEntity> {

}
