package io.github.loncra.basic.service.message.server.service.chat.call;

import io.github.loncra.basic.service.message.server.dao.chat.call.UserChatCallDao;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *
 * tb_user_chat_call 的业务逻辑
 *
 * <p>Table: tb_user_chat_call - 聊天通话表</p>
 *
 * @see UserChatCallEntity
 *
 * @author maurice.chen
 *
 * @since 2026-06-30 08:37:17
 */
@Service
@RequiredArgsConstructor
public class UserChatCallService extends BasicService<UserChatCallDao, UserChatCallEntity> {

}
