package io.github.loncra.basic.service.auth.server.service.user.personal;

import io.github.loncra.basic.service.auth.api.constants.AuthenticationMqConstants;
import io.github.loncra.basic.service.auth.server.dao.user.PersonalUserDao;
import io.github.loncra.basic.service.auth.server.domain.body.PersonalUserRegisterRequestBody;
import io.github.loncra.basic.service.auth.server.domain.entity.user.PersonalUserEntity;
import io.github.loncra.basic.service.commons.config.CommonsConfig;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.ExportDataMetadata;
import io.github.loncra.framework.captcha.ReceivingTargetSimpleCaptcha;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.enumerate.security.UserStatus;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.generator.twitter.SnowflakeIdGenerator;
import io.github.loncra.framework.idempotent.annotation.Concurrent;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

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

    private final RedissonClient redissonClient;

    private final AmqpTemplate amqpTemplate;

    private final CommonsConfig commonsConfig;

    private final SnowflakeIdGenerator snowflakeIdGenerator;

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

    @Transactional(rollbackFor = Exception.class)
    @Concurrent(value = "registerPersonalUser:[#body.phoneNumber]")
    public PersonalUserEntity register(
            PersonalUserRegisterRequestBody body,
            ReceivingTargetSimpleCaptcha captcha
    ) {
        Assert.notNull(captcha, "找不到短信验证码校验结果");
        Assert.isTrue(
                Strings.CS.equals(body.getPhoneNumber(), captcha.getTarget()),
                "短信验证码接收手机号与注册手机号不一致"
        );
        Assert.isTrue(
                Strings.CS.equals(body.getPassword(), body.getConfirmPassword()),
                "密码与确认密码不一致"
        );

        boolean exists = lambdaQuery()
                .eq(PersonalUserEntity::getPhoneNumber, body.getPhoneNumber())
                .exists();
        SystemException.isTrue(!exists, "该手机号已注册");

        PersonalUserEntity user = new PersonalUserEntity();
        user.setUsername(commonsConfig.generateRandomUsername(body.getPhoneNumber()));
        user.setNickname(StringUtils.defaultIfBlank(body.getNickname(), commonsConfig.generateRandomNickName()));
        user.setPassword(passwordEncoder.encode(body.getPassword()));
        user.setStatus(UserStatus.Enabled);
        user.setPhoneNumber(body.getPhoneNumber());
        user.setPhoneNumberVerified(YesOrNo.Yes);
        user.setTenantId(snowflakeIdGenerator.generateId());
        user.getInitialization().setRandomPassword(YesOrNo.No);

        insert(user);
        return user;
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
}
