package io.github.loncra.basic.service.auth.server.service.user.personal;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import io.github.loncra.basic.service.auth.api.constants.AuthenticationMqConstants;
import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.dao.user.PersonalUserDao;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.RoleEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.user.PersonalUserEntity;
import io.github.loncra.basic.service.auth.server.security.AbstractSystemUserDetailsService;
import io.github.loncra.basic.service.auth.server.service.role.RoleService;
import io.github.loncra.basic.service.commons.config.CommonsConfig;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.ExportDataMetadata;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * tb_personal_user 的业务逻辑
 *
 * <p>Table: tb_personal_user - 个人用户表</p>
 *
 * @see PersonalUserEntity
 *
 * @author maurice.chen
 *
 * @since 2026-03-28 09:46:07
 */
@Data
@Service
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PersonalUserService extends BasicService<PersonalUserDao, PersonalUserEntity> {

    @Getter
    private final RoleService roleService;

    private final PasswordEncoder passwordEncoder;

    private final RedissonClient redissonClient;

    private final AmqpTemplate amqpTemplate;

    private final CommonsConfig commonsConfig;

    public PersonalUserEntity getByIdentity(String identity) {
        return lambdaQuery().eq(PersonalUserEntity::getId, identity)
                .or()
                .eq(PersonalUserEntity::getUsername, identity)
                .or()
                .eq(PersonalUserEntity::getEmail, identity)
                .or()
                .eq(PersonalUserEntity::getPhoneNumber, identity)
                .one();
    }

    @Override
    public int insert(PersonalUserEntity entity) {
        SystemException.isTrue(!lambdaQuery().eq(AbstractBasicSystemUser::getUsername, entity.getUsername()).exists(), "登录账户已存在");
        if (StringUtils.isNotEmpty(entity.getPhoneNumber())) {
            SystemException.isTrue(!lambdaQuery().eq(PersonalUserEntity::getPhoneNumber, entity.getPhoneNumber()).exists(), "手机号码已存在");
        }
        if (StringUtils.isNotEmpty(entity.getEmail())) {
            SystemException.isTrue(!lambdaQuery().eq(PersonalUserEntity::getEmail, entity.getEmail()).exists(), "邮箱已存在");
        }
        return super.insert(entity);
    }

    public void export(ExportDataMetadata dto) {
        String cacheName = SystemConstants.USER_EXPORT_CACHE.getName(dto.toExportCacheName());
        RBucket<ExportDataMetadata> bucket = redissonClient.getBucket(cacheName);
        if (bucket.isExists()) {
            return ;
        }
        bucket.set(dto, SystemConstants.USER_EXPORT_CACHE.getExpiresTime().toDuration());
        amqpTemplate.convertAndSend(SystemConstants.SYS_AUTH_RABBITMQ_EXCHANGE, AuthenticationMqConstants.PERSONAL_USER_EXPORT_QUEUE_NAME, dto.toExportCacheName());
    }

    public List<ResourceEntity> getResource(
            AuditAuthenticationToken token,
            List<ResourceTypeEnum> list,
            List<ResourceSourceEnum> sourceContains
    ) {
        PersonalUserEntity user = get(token.getSecurityPrincipal().getId().toString());
        List<RoleEntity> roles = roleService.get(user.getRoleIds());
        Set<Long> resourceIds = roles.stream()
                .flatMap(s -> s.getResourceIds().stream()).collect(Collectors.toSet());
        return roleService.getSystemUserResource(resourceIds, list, sourceContains);
    }

    public List<PersonalUserEntity> findByRoleIds(Set<Long> roleIds) {
        Map<String, Object> filter = Map.of("filter_[role_ids_jin]", roleIds);
        Wrapper<PersonalUserEntity> wrapper = getQueryGenerator().createQueryWrapperFromMap(filter);
        return find(wrapper);
    }

    public Collection<SimpleGrantedAuthority> getAuthorities(PersonalUserEntity user) {
        List<RoleEntity> roles = roleService
                .get(user.getRoleIds());
        if (CollectionUtils.isNotEmpty(user.getRoleIds())) {
            return List.of();
        }
        Set<Long> resourceIds = roles.stream()
                .flatMap(s -> s.getResourceIds().stream()).collect(Collectors.toSet());
        List<ResourceEntity> resources = roleService
                .getSystemUserResource(resourceIds, List.of(), List.of(ResourceSourceEnum.PERSONAL));
        return AbstractSystemUserDetailsService.createGrantedAuthorities(new LinkedList<>(roles), resources);
    }
}
