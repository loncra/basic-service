package io.github.loncra.basic.service.auth.server.service.enterprise;

import io.github.loncra.basic.service.auth.server.dao.enterprise.EnterpriseInvitationDao;
import io.github.loncra.basic.service.auth.server.domain.body.EnterpriseInvitationResponse;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseInvitationEntity;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseInvitationStatusEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.security.entity.RoleAuthority;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * tb_enterprise_invitation 的业务逻辑
 *
 * <p>Table: tb_enterprise_invitation - 企业邀请表</p>
 *
 * @author maurice.chen
 * @see EnterpriseInvitationEntity
 */
@Service
@RequiredArgsConstructor
public class EnterpriseInvitationService extends BasicService<EnterpriseInvitationDao, EnterpriseInvitationEntity> {

    private final EnterpriseMemberService enterpriseMemberService;

    public List<EnterpriseInvitationEntity> findPendingByEnterpriseId(Long enterpriseId) {
        return lambdaQuery()
                .eq(EnterpriseInvitationEntity::getEnterpriseId, enterpriseId)
                .eq(EnterpriseInvitationEntity::getStatus, EnterpriseInvitationStatusEnum.EXECUTION)
                .list();
    }

    public EnterpriseInvitationResponse convertResponseBody(EnterpriseInvitationEntity entity) {
        EnterpriseInvitationResponse result = CastUtils.of(entity, EnterpriseInvitationResponse.class);

        if (CollectionUtils.isNotEmpty(entity.getRoleIds())) {
            enterpriseMemberService.getEnterpriseRoleService()
                    .get(entity.getRoleIds())
                    .stream().map(s -> new RoleAuthority(s.getName(), s.getAuthority()))
                    .forEach(s -> result.getRoles().add(s));
        }

        if (StringUtils.isNotEmpty(entity.getPrincipal())) {
            TypeIdNameMetadata metadata = TypeIdNameMetadata.ofPrincipalString(entity.getPrincipal());
            result.setMember(enterpriseMemberService.get(metadata.getId()));
        }

        if (Objects.nonNull(result.getMember())) {
            enterpriseMemberService.setPersonalUser(result.getMember());
        }

        return result;
    }
}
