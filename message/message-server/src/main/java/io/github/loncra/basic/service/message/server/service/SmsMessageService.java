package io.github.loncra.basic.service.message.server.service;

import io.github.loncra.basic.service.message.server.dao.SmsMessageDao;
import io.github.loncra.basic.service.message.server.domain.entity.SmsMessageEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import org.springframework.stereotype.Service;

/**
 * tb_sms_message 的业务逻辑
 *
 * <p>Table: tb_sms_message - 短信消息</p>
 *
 * @author maurice.chen
 * @see SmsMessageEntity
 * @since 2021-12-10 09:02:07
 */
@Service
public class SmsMessageService extends BasicService<SmsMessageDao, SmsMessageEntity> {
}
