package io.github.loncra.basic.service.message.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.message.server.domain.entity.SmsMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_sms_message 短信消息数据访问
 *
 * <p>Table: tb_sms_message - 短信消息</p>
 *
 * @author maurice
 * @see SmsMessageEntity
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface SmsMessageDao extends BaseMapper<SmsMessageEntity> {

}
