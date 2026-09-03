package io.github.loncra.basic.service.auth.server.service.enterprise;

import io.github.loncra.basic.service.auth.server.dao.enterprise.EnterpriseInvitationDao;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseInvitationEntity;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseInvitationStatusEnum;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public EnterpriseInvitationEntity getByCode(String code) {
        return lambdaQuery()
                .eq(EnterpriseInvitationEntity::getCode, code)
                .one();
    }

    public EnterpriseInvitationEntity getPendingInvitation(
            Long enterpriseId,
            String phoneNumber
    ) {
        return lambdaQuery()
                .eq(EnterpriseInvitationEntity::getEnterpriseId, enterpriseId)
                .eq(EnterpriseInvitationEntity::getPhoneNumber, phoneNumber)
                .eq(EnterpriseInvitationEntity::getStatus, EnterpriseInvitationStatusEnum.PENDING)
                .one();
    }

    public List<EnterpriseInvitationEntity> findPendingByEnterpriseId(Long enterpriseId) {
        return lambdaQuery()
                .eq(EnterpriseInvitationEntity::getEnterpriseId, enterpriseId)
                .eq(EnterpriseInvitationEntity::getStatus, EnterpriseInvitationStatusEnum.PENDING)
                .list();
    }
}
