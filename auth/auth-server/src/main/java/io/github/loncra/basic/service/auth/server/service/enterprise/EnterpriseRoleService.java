package io.github.loncra.basic.service.auth.server.service.enterprise;

import io.github.loncra.basic.service.auth.server.dao.enterprise.EnterpriseRoleDao;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseRoleEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *
 * tb_enterprise_role 的业务逻辑
 *
 * <p>Table: tb_enterprise_role - 企业用户组表</p>
 *
 * @see EnterpriseRoleEntity
 *
 * @author maurice.chen
 *
 * @since 2026-09-05 06:30:36
 */
@Service
@RequiredArgsConstructor
public class EnterpriseRoleService extends BasicService<EnterpriseRoleDao, EnterpriseRoleEntity> {

}
