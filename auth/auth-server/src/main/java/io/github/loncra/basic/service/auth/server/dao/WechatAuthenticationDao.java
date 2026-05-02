package io.github.loncra.basic.service.auth.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.auth.server.domain.entity.WechatAuthenticationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_wechat_authentication 的数据访问
 *
 * <p>Table: tb_wechat_authentication - 第三方认证信息</p>
 *
 * @author maurice.chen
 * @see WechatAuthenticationEntity
 * @since 2025-05-08 03:39:57
 */
@Mapper
@Repository
public interface WechatAuthenticationDao extends BaseMapper<WechatAuthenticationEntity> {

}
