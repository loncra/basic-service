package io.github.loncra.basic.service.auth.server.dao.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.auth.server.domain.entity.user.PersonalUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_personal_user 的数据访问
 *
 * <p>Table: tb_personal_user - 个人用户表</p>
 *
 * @see PersonalUserEntity
 *
 * @author maurice.chen
 *
 * @since 2026-03-28 09:46:07
 */
@Mapper
@Repository
public interface PersonalUserDao extends BaseMapper<PersonalUserEntity> {

}
