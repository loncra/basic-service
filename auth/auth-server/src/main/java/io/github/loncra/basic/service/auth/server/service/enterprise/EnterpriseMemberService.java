package io.github.loncra.basic.service.auth.server.service.enterprise;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.dao.enterprise.EnterpriseMemberDao;
import io.github.loncra.basic.service.auth.server.domain.BasicSystemRole;
import io.github.loncra.basic.service.auth.server.domain.body.EnterpriseMemberResponseBody;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.RoleEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseMemberEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseRoleEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.user.PersonalUserEntity;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseMemberRoleEnum;
import io.github.loncra.basic.service.auth.server.security.AbstractSystemUserDetailsService;
import io.github.loncra.basic.service.auth.server.service.user.personal.PersonalUserService;
import io.github.loncra.basic.service.commons.domain.metadata.AuditMetadata;
import io.github.loncra.basic.service.commons.enumerate.AuditStatusEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.security.UserStatus;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.security.entity.RoleAuthority;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.core.GrantedAuthority;
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
@Getter
@Service
@RequiredArgsConstructor
public class EnterpriseMemberService extends BasicService<EnterpriseMemberDao, EnterpriseMemberEntity> {

    private final PersonalUserService personalUserService;

    private final EnterpriseRoleService enterpriseRoleService;

    //private final MessageServiceClient messageServiceClient;

    public EnterpriseMemberEntity getActiveMember(
            Long enterpriseId,
            String principal
    ) {
        return lambdaQuery()
                .eq(EnterpriseMemberEntity::getEnterpriseId, enterpriseId)
                .eq(EnterpriseMemberEntity::getPrincipal, principal)
                .eq(EnterpriseMemberEntity::getAuditStatus, AuditStatusEnum.AGREED.getValue())
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
        List<BasicSystemRole> roles = getRole(entity);
        if (CollectionUtils.isNotEmpty(roles) && CollectionUtils.isEmpty(entity.getResourceIds())) {
            List<ResourceEntity> resourceAuthorities = personalUserService.getRoleService()
                    .getSystemUserResource(roles.stream()
                                    .flatMap(s -> s.getResourceIds()
                                            .stream()
                                    ).collect(Collectors.toSet()),
                            List.of(),
                            List.of(ResourceSourceEnum.ENTERPRISE)
                    );
            entity.setResourceIds(resourceAuthorities.stream().map(ResourceEntity::getId).collect(Collectors.toSet()));
        }
    }

    public List<EnterpriseMemberEntity> findActiveByPrincipal(String principal) {
        return lambdaQuery()
                .eq(EnterpriseMemberEntity::getPrincipal, principal)
                .eq(EnterpriseMemberEntity::getAuditStatus, AuditStatusEnum.AGREED.getValue())
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
        Set<Long> resourceIds = user.getResourceIds();
        if (CollectionUtils.isEmpty(resourceIds)) {
            List<BasicSystemRole> roles = getRole(user);
            resourceIds = roles.stream()
                    .flatMap(s -> s.getResourceIds().stream())
                    .collect(Collectors.toSet());
        }

        return personalUserService.getRoleService()
                .getSystemUserResource(resourceIds, list, sourceContains);

    }

    public List<BasicSystemRole> getRole(EnterpriseMemberEntity enterpriseMember) {
        List<BasicSystemRole> roleAuthorities = new LinkedList<>();
        if (EnterpriseMemberRoleEnum.OWNER.equals(enterpriseMember.getRole())) {
            RoleEntity role = personalUserService.getRoleService()
                    .getByAuthority(ResourceSourceEnum.ENTERPRISE.toString());
            roleAuthorities.add(role);
        }
        if (CollectionUtils.isNotEmpty(enterpriseMember.getRoleIds())) {
            List<EnterpriseRoleEntity> enterpriseRoles = enterpriseRoleService.get(enterpriseMember.getRoleIds());
            roleAuthorities.addAll(enterpriseRoles);
        }
        return roleAuthorities;
    }

    public Collection<GrantedAuthority> getAuthorities(
            List<BasicSystemRole> roles,
            EnterpriseMemberRoleEnum role,
            Set<Long> resourceIds
    ) {
        Set<Long> roleResourceIds = roles.stream()
                .flatMap(s -> s.getResourceIds().stream())
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(resourceIds)) {
            roleResourceIds.addAll(resourceIds);
        }

        List<ResourceEntity> resourceAuthorities = personalUserService.getRoleService()
                .getSystemUserResource(
                        roleResourceIds,
                        List.of(),
                        List.of(ResourceSourceEnum.ENTERPRISE)
                );

        List<RoleAuthority> roleAuthorities = roles.stream()
                .map(s -> new RoleAuthority(s.getName(), s.getAuthority()))
                .toList();
        Collection<GrantedAuthority> authorities = AbstractSystemUserDetailsService.createGrantedAuthorities(roleAuthorities, resourceAuthorities);

        authorities.add(new SimpleGrantedAuthority(EnterpriseMemberRoleEnum.SECURITY_ROLE_PREFIX + role));

        return authorities;
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
        owner.setAuditStatus(AuditStatusEnum.AGREED);
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

    @Transactional(rollbackFor = Exception.class)
    public void audit(
            List<Long> ids,
            AuditMetadata metadata,
            AuditAuthenticationToken token
    ) {
        List<EnterpriseMemberEntity> members = get(ids);
        members.forEach(e -> audit(e, metadata, token));
    }

    @Transactional(rollbackFor = Exception.class)
    public void audit(
            EnterpriseMemberEntity member,
            AuditMetadata metadata,
            AuditAuthenticationToken token
    ) {
        if (!AuditStatusEnum.AUDITABLE.equals(member.getAuditStatus())) {
            return ;
        }
        member.setAuditStatus(metadata.getStatus());
        member.setRemark(metadata.getRemark());

        if (AuditStatusEnum.AGREED.equals(member.getAuditStatus())) {
            member.setStatus(UserStatus.Enabled);
        }

        updateById(member);
    }

    public EnterpriseMemberEntity convertResponseBody(EnterpriseMemberEntity memberEntity) {
        EnterpriseMemberResponseBody body = CastUtils.of(memberEntity, EnterpriseMemberResponseBody.class);
        if (CollectionUtils.isNotEmpty(body.getRoleIds())) {
            List<RoleAuthority> roles = enterpriseRoleService.get(memberEntity.getRoleIds())
                    .stream()
                    .filter(Objects::nonNull)
                    .map(s -> new RoleAuthority(s.getName(), s.getAuthority()))
                    .toList();
            body.setRoles(roles);
        }
        return body;
    }
}
