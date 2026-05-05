package io.github.loncra.basic.service.auth.server.service.user.console;

import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.config.AuthAppConfig;
import io.github.loncra.basic.service.auth.server.dao.user.ConsoleUserDao;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.user.ConsoleUserEntity;
import io.github.loncra.basic.service.auth.server.service.role.RoleService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.id.metadata.IdNameValueMetadata;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * tb_console_user 的业务逻辑
 *
 * <p>Table: tb_console_user - 后台用户表</p>
 *
 * @author maurice.chen
 * @see ConsoleUserEntity
 * @since 2021-11-25 02:42:57
 */
@Data
@Service
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ConsoleUserService extends BasicService<ConsoleUserDao, ConsoleUserEntity> {

    private final AuthAppConfig authAppConfig;

    private final PasswordEncoder passwordEncoder;

    private final RoleService roleService;

    @Override
    public int deleteByEntity(ConsoleUserEntity entity) {

        Assert.isTrue(!Strings.CS.equals(entity.getUsername(), authAppConfig.getAdminUsername()), "管理员用户不能删除");
        List<IdNameValueMetadata<String, List<ResourceSourceEnum>>> authorities = authAppConfig.getAutoAssociateAllPermissionsRoleAuthorities();
        boolean deletable = entity.getRoleIds()
                .stream()
                .map(roleService::get)
                .noneMatch(r -> authorities.stream().noneMatch(a -> a.getId().equals(r.getAuthority())));
        Assert.isTrue(deletable, "管理员角色用户不能删除");

        return super.deleteByEntity(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(ConsoleUserEntity entity) {
        Assert.isTrue(!Strings.CS.equals(entity.getUsername(), authAppConfig.getAdminUsername()), "管理员用户不能修改");
        return super.updateById(entity);
    }

    @Override
    public int insert(ConsoleUserEntity entity) {
        Assert.hasText(entity.getUsername(), "登陆账户不能为空");

        boolean usernameExist = lambdaQuery()
                .select(ConsoleUserEntity::getId)
                .eq(ConsoleUserEntity::getUsername, entity.getUsername())
                .exists();
        Assert.isTrue(!usernameExist, "登录账户 [" + entity.getUsername() + "] 已存在");

        if (StringUtils.isNotBlank(entity.getEmail())) {
            boolean emailExist = lambdaQuery()
                    .select(ConsoleUserEntity::getId)
                    .eq(ConsoleUserEntity::getEmail, entity.getEmail())
                    .exists();
            Assert.isTrue(!emailExist, "邮箱账户 [" + entity.getEmail() + "] 已存在");
        }

        if (StringUtils.isNotBlank(entity.getPhoneNumber())) {
            boolean phoneNumberExist = lambdaQuery()
                    .select(ConsoleUserEntity::getId)
                    .eq(ConsoleUserEntity::getPhoneNumber, entity.getPhoneNumber())
                    .exists();
            Assert.isTrue(!phoneNumberExist, "手机号码 [" + entity.getPhoneNumber() + "] 已存在");
        }

        entity.setPassword(passwordEncoder.encode(entity.getPassword()));

        return super.insert(entity);
    }

    public ConsoleUserEntity getByIdentity(String identity) {
        return lambdaQuery().eq(ConsoleUserEntity::getId, identity)
                .or()
                .eq(ConsoleUserEntity::getUsername, identity)
                .or()
                .eq(ConsoleUserEntity::getEmail, identity)
                .one();
    }


    public List<ResourceEntity> getResource(
            AuditAuthenticationToken token,
            List<ResourceTypeEnum> types,
            List<ResourceSourceEnum> sourceContains
    ) {
        ConsoleUserEntity user = get(token.getSecurityPrincipal().getId().toString());

        List<ResourceEntity> userResource = roleService
                .getPluginResourceService()
                .getResources()
                .stream()
                .filter(r -> user.getResourceIds().contains(r.getId()))
                .toList();

        Stream<ResourceEntity> stream = userResource
                .stream()
                .filter(r -> r.getSources().stream().anyMatch(sourceContains::contains));

        if (Objects.nonNull(token.getType())) {
            stream = stream.filter(r -> types.contains(r.getType()));
        }

        return stream.toList();
    }
}
