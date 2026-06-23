package io.github.loncra.basic.service.auth.server.service.user.personal;

import io.github.loncra.basic.service.auth.server.consumer.PersonalUserConsumer;
import io.github.loncra.basic.service.auth.server.dao.user.PersonalUserDao;
import io.github.loncra.basic.service.auth.server.domain.entity.user.PersonalUserEntity;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.ExportDataMetadata;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
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

    private final RedissonClient redissonClient;

    private final AmqpTemplate amqpTemplate;

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

    public void export(ExportDataMetadata dto) {
        String cacheName = SystemConstants.USER_EXPORT_CACHE.getName(dto.toExportCacheName());
        RBucket<ExportDataMetadata> bucket = redissonClient.getBucket(cacheName);
        if (bucket.isExists()) {
            return ;
        }
        bucket.set(dto, SystemConstants.USER_EXPORT_CACHE.getExpiresTime().toDuration());
        amqpTemplate.convertAndSend(SystemConstants.SYS_AUTH_RABBITMQ_EXCHANGE, PersonalUserConsumer.DEFAULT_EXPORT_QUEUE_NAME, dto.toExportCacheName());
    }
}
