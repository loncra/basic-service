package io.github.loncra.basic.service.auth.server.dao.merchant;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.auth.server.domain.entity.merchant.OpenPlatformMerchantClientEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_open_platform_merchant_client 商家 OAuth 2 客户端注册信息的数据访问
 *
 * <p>Table: tb_open_platform_merchant_client 商家 OAuth 2 客户端注册信息- </p>
 *
 * @author maurice.chen
 * @see OpenPlatformMerchantClientEntity
 * @since 2023-11-22 03:21:13
 */
@Mapper
@Repository
public interface OpenPlatformMerchantClientDao extends BaseMapper<OpenPlatformMerchantClientEntity> {

}
