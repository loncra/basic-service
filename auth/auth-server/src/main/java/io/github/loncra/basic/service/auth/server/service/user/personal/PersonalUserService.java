package io.github.loncra.basic.service.auth.server.service.user.personal;

import io.github.loncra.basic.service.auth.server.dao.user.PersonalUserDao;
import io.github.loncra.basic.service.auth.server.domain.entity.user.PersonalUserEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    private final PasswordEncoder passwordEncoder;

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
}
