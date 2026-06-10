package io.github.loncra.basic.service.message.server.resolver;

import io.github.loncra.basic.service.message.server.domain.body.chat.UserChatConversationResponseBody;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatParticipantEntity;
import io.github.loncra.basic.service.message.server.enumerate.UserChatRoomBuisnessScenEnum;
import io.github.loncra.framework.socketio.api.ReturnValueSocketResult;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;

import java.util.List;

/**
 * 聊天房间解析器
 *
 * @author maurice.chen
 */
public interface ChatRoomResolver {

    /**
     * 获取聊天房间业务场景
     *
     * @return 聊天房间业务场景
     */
    UserChatRoomBuisnessScenEnum getBusinessScene();

    /**
     * 创建房间之后触发此方法
     * @param result 响应内容
     * @param participantList 参与者
     * @param token 认证 token
     */
    void postCreate(
            ReturnValueSocketResult<UserChatConversationResponseBody> result,
            List<UserChatParticipantEntity> participantList,
            AuditAuthenticationToken token
    );
}
