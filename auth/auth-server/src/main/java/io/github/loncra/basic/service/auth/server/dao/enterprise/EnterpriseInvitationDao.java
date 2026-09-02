package io.github.loncra.basic.service.auth.server.dao.enterprise;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseInvitationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_enterprise_invitation 的数据访问
 *
 * <p>Table: tb_enterprise_invitation - 企业邀请表</p>
 *
 * @author maurice.chen
 * @see EnterpriseInvitationEntity
 */
@Mapper
@Repository
public interface EnterpriseInvitationDao extends BaseMapper<EnterpriseInvitationEntity> {

}
