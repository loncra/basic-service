package io.github.loncra.basic.service.message.server.service;

import io.github.loncra.basic.service.message.server.dao.BatchMessageDao;
import io.github.loncra.basic.service.message.server.domain.entity.BatchMessageEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import org.springframework.stereotype.Service;

/**
 * tb_batch_message 的业务逻辑
 *
 * <p>Table: tb_batch_message - 批量消息</p>
 *
 * @author maurice.chen
 * @see BatchMessageEntity
 * @since 2021-12-10 09:02:07
 */
@Service
public class BatchMessageService extends BasicService<BatchMessageDao, BatchMessageEntity> {

}
