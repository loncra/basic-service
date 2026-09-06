package io.github.loncra.basic.service.auth.server.service.enterprise;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.dao.enterprise.EnterpriseMemberDao;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.RoleEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseMemberEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.user.PersonalUserEntity;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseMemberInvitationEnum;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseMemberRoleEnum;
import io.github.loncra.basic.service.auth.server.security.AbstractSystemUserDetailsService;
import io.github.loncra.basic.service.auth.server.service.user.personal.PersonalUserService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.enumerate.security.UserStatus;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * tb_enterprise_member 的业务逻辑
 *
 * <p>Table: tb_enterprise_member - 企业成员表</p>
 *
 * @author maurice.chen
 * @see EnterpriseMemberEntity
 */
@Service
@RequiredArgsConstructor
public class EnterpriseMemberService extends BasicService<EnterpriseMemberDao, EnterpriseMemberEntity> {

    @Getter
    private final PersonalUserService personalUserService;

    public EnterpriseMemberEntity getActiveMember(
            Long enterpriseId,
            String principal
    ) {
        return lambdaQuery()
                .eq(EnterpriseMemberEntity::getEnterpriseId, enterpriseId)
                .eq(EnterpriseMemberEntity::getPrincipal, principal)
                .eq(EnterpriseMemberEntity::getInvitation, EnterpriseMemberInvitationEnum.ACTIVE.getValue())
                .eq(EnterpriseMemberEntity::getStatus, UserStatus.Enabled.getValue())
                .one();
    }

    public EnterpriseMemberEntity getMember(
            Long enterpriseId,
            String principal
    ) {
        return lambdaQuery()
                .eq(EnterpriseMemberEntity::getEnterpriseId, enterpriseId)
                .eq(EnterpriseMemberEntity::getPrincipal, principal)
                .one();
    }

    public void setPersonalUser(EnterpriseMemberEntity entity) {
        TypeIdNameMetadata metadata = TypeIdNameMetadata.ofPrincipalString(entity.getPrincipal());
        PersonalUserEntity user = personalUserService.get(metadata.getId());
        entity.setPersonalUser(user);
    }

    public List<EnterpriseMemberEntity> findActiveByPrincipal(String principal) {
        return lambdaQuery()
                .eq(EnterpriseMemberEntity::getPrincipal, principal)
                .eq(EnterpriseMemberEntity::getInvitation, EnterpriseMemberInvitationEnum.ACTIVE.getValue())
                .eq(EnterpriseMemberEntity::getStatus, UserStatus.Enabled.getValue())
                .list();
    }

    public List<EnterpriseMemberEntity> findByEnterpriseId(Long enterpriseId) {
        return lambdaQuery()
                .eq(EnterpriseMemberEntity::getEnterpriseId, enterpriseId)
                .list();
    }

    public Collection<EnterpriseMemberEntity> findByRoleIds(Set<Long> roleIds) {
        Map<String, Object> filter = Map.of("filter_[role_ids_jin]", roleIds);
        Wrapper<EnterpriseMemberEntity> wrapper = getQueryGenerator().createQueryWrapperFromMap(filter);
        return find(wrapper);
    }

    public List<ResourceEntity> getResource(
            AuditAuthenticationToken token,
            List<ResourceTypeEnum> list,
            List<ResourceSourceEnum> sourceContains
    ) {
        EnterpriseMemberEntity user = get(token.getSecurityPrincipal().getId().toString());
        List<RoleEntity> roles = personalUserService.getRoleService()
                .get(user.getRoleIds());
        Set<Long> resourceIds = roles.stream()
                .flatMap(s -> s.getResourceIds().stream()).collect(Collectors.toSet());
        List<ResourceEntity> result = new LinkedList<>(personalUserService.getRoleService()
                .getSystemUserResource(resourceIds, list, sourceContains));

        // TODO 待完成企业内部权限获取问题
        return result;
    }

    public Collection<SimpleGrantedAuthority> getAuthorities(EnterpriseMemberEntity enterpriseMember) {
        List<RoleEntity> roles = personalUserService.getRoleService()
                .get(enterpriseMember.getRoleIds());
        if (CollectionUtils.isEmpty(enterpriseMember.getRoleIds())) {
            return List.of();
        }

        List<ResourceEntity> resources = roles.stream()
                .flatMap(s -> personalUserService.getRoleService().getGroupResource(s).stream())
                .toList();
        return AbstractSystemUserDetailsService.createGrantedAuthorities(roles, resources);
    }

    @Transactional(rollbackFor = Exception.class)
    public EnterpriseMemberEntity createOwner(
            EnterpriseEntity enterprise,
            String principal
    ) {
        EnterpriseMemberEntity owner = new EnterpriseMemberEntity();
        owner.setEnterpriseId(enterprise.getId());
        owner.setPrincipal(principal);
        owner.setRole(EnterpriseMemberRoleEnum.OWNER);
        owner.setStatus(UserStatus.Enabled);
        owner.setInvitation(EnterpriseMemberInvitationEnum.ACTIVE);
        owner.setLastAuthenticationTime(Instant.now());
        owner.setTenantId(enterprise.getTenantId());

        TypeIdNameMetadata metadata = TypeIdNameMetadata.ofPrincipalString(principal);
        PersonalUserEntity personalUser = personalUserService.getByIdentity(metadata.getId());
        owner.setUsername(personalUser.getUsername());
        owner.setPassword(personalUser.getPassword());

        owner.setPersonalUser(personalUser);

        RoleEntity defaultRole = personalUserService
                .getRoleService()
                .getByAuthority(ResourceSourceEnum.ENTERPRISE.getAdminAuthority().getId());
        if (Objects.nonNull(defaultRole)) {
            owner.setRoleIds(Set.of(defaultRole.getId()));
        }

        insert(owner);

        return owner;
    }
}
