package io.github.loncra.basic.service.auth.server.service.merchant;

import io.github.loncra.basic.service.auth.server.dao.merchant.OpenPlatformMerchantClientDao;
import io.github.loncra.basic.service.auth.server.domain.entity.merchant.OpenPlatformMerchantClientEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * tb_open_platform_merchant_client 商家 OAuth 2 客户端注册信息的业务逻辑
 *
 * <p>Table: tb_open_platform_merchant_client 商家 OAuth 2 客户端注册信息- </p>
 *
 * @author maurice.chen
 * @see OpenPlatformMerchantClientEntity
 * @since 2023-11-22 03:21:13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenPlatformMerchantClientService extends BasicService<OpenPlatformMerchantClientDao, OpenPlatformMerchantClientEntity> {

    public List<OpenPlatformMerchantClientEntity> getByMerchantId(Long merchantId) {
        return lambdaQuery().eq(OpenPlatformMerchantClientEntity::getMerchantId, merchantId)
                .list();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteByMerchantId(Long id) {
        lambdaQuery().eq(OpenPlatformMerchantClientEntity::getMerchantId, id)
                .list()
                .forEach(this::deleteByEntity);
    }

}
