
package io.github.loncra.basic.service.auth.server.dao.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.auth.server.domain.entity.user.ConsoleUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_console_user 系统用户数据访问
 *
 * <p>Table: tb_console_user - 系统用户</p>
 *
 * @author maurice
 * @see ConsoleUserEntity
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface ConsoleUserDao extends BaseMapper<ConsoleUserEntity> {

}
