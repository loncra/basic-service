package io.github.loncra.basic.service.auth.server.dao.merchant;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.auth.server.domain.entity.merchant.OpenPlatformMerchantEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_open_platform_merchant 的数据访问
 *
 * <p>Table: tb_open_platform_merchant - 开放平台商户表</p>
 *
 * @author maurice.chen
 * @see OpenPlatformMerchantEntity
 * @since 2023-09-11 08:57:11
 */
@Mapper
@Repository
public interface OpenPlatformMerchantDao extends BaseMapper<OpenPlatformMerchantEntity> {

}
