package io.github.loncra.basic.service.auth.server.service.user.console;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.config.AuthAppConfig;
import io.github.loncra.basic.service.auth.server.dao.user.ConsoleUserDao;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.user.ConsoleUserEntity;
import io.github.loncra.basic.service.auth.server.service.role.RoleService;
import io.github.loncra.basic.service.commons.config.CommonsConfig;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
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

import java.io.Serializable;
import java.util.*;
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

    private final CommonsConfig commonsConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(
            Collection<? extends Serializable> ids,
            boolean errorThrow,
            boolean useFill
    ) {

        int result = ids.stream().mapToInt(id -> deleteById(id, useFill)).sum();
        if (result != ids.size() && errorThrow) {
            String msg = "删除 id 为 [" + ids + "] 的 [" + ResourceSourceEnum.CONSOLE.getName() + "] 失败";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    public int deleteByEntity(
            Collection<ConsoleUserEntity> entities,
            boolean errorThrow
    ) {
        int result = entities.stream().mapToInt(this::deleteByEntity).sum();
        if (result != entities.size() && errorThrow) {
            String msg = "删除 id 为 [" + entities.stream().map(IdEntity::getId).toList() + "] 的 [" + ResourceSourceEnum.CONSOLE.getName() + "] 失败";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    public int delete(Wrapper<ConsoleUserEntity> wrapper) {
        throw new UnsupportedOperationException(ResourceSourceEnum.CONSOLE.getName() + "不支持 delete 操作");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(
            Serializable id,
            boolean useFill
    ) {
        return deleteByEntity(get(id));
    }

    @Override
    public int deleteByEntity(ConsoleUserEntity entity) {

        if (Objects.nonNull(ResourceSourceEnum.CONSOLE.getAdminAuthority())) {
            Assert.isTrue(!Strings.CS.equals(entity.getUsername(), ResourceSourceEnum.CONSOLE.getAdminAuthority().getValue()), "管理员用户不能删除");
        }
        boolean deletable = entity.getRoleIds()
                .stream()
                .map(roleService::get)
                .noneMatch(r ->  ResourceSourceEnum.CONSOLE.getAdminAuthority().getId().equals(r.getAuthority()));
        Assert.isTrue(deletable, "管理员角色用户不能删除");

        return super.deleteByEntity(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(ConsoleUserEntity entity) {
        if (Objects.nonNull(ResourceSourceEnum.CONSOLE.getAdminAuthority())) {
            Assert.isTrue(!Strings.CS.equals(entity.getUsername(), ResourceSourceEnum.CONSOLE.getAdminAuthority().getValue()), "管理员用户不能修改");
        }
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

        entity.setPassword(passwordEncoder.encode(commonsConfig.generateRandomPassword()));

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

    public List<ConsoleUserEntity> findByRoleIds(Set<Long> roleIds) {
        Map<String, Object> filter = Map.of("filter_[role_ids_jin]", roleIds);
        Wrapper<ConsoleUserEntity> wrapper = getQueryGenerator().createQueryWrapperFromMap(filter);
        return find(wrapper);
    }
}
