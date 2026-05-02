package io.github.loncra.basic.service.auth.server.resolver;


import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.server.domain.BasicSystemRole;

import java.util.List;

/**
 * 系统用户角色发生变更解析器
 *
 * @author maurice.chen
 */
public interface SystemUserRoleSyncResolver {

    /**
     * 同步系统用户组
     *
     * @param sue    系统用户
     * @param groups 组信息
     */
    void syncSystemUserGroup(
            AbstractBasicSystemUser sue,
            List<BasicSystemRole> groups
    );
}
