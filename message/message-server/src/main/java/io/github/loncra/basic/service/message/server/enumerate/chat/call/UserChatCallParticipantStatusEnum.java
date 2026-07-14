package io.github.loncra.basic.service.message.server.enumerate.chat.call;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 通话参与者状态
 *
 * @author maurice.chen
 *
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserChatCallParticipantStatusEnum implements NameValueEnum<Integer> {

    // ========== 基础呼叫阶段 ==========
    INITIATING(10, "呼叫中"),
    RINGING(20, "响铃中"),

    // ========== 通话建立阶段 ==========
    CONNECTING(30, "连接中"),
    ACTIVE(40, "通话中"),

    // ========== 结束阶段 ==========
    COMPLETED(50, "已挂断"),
    COMPLETED_BY_CALLER(51, "主叫结束通话"),
    COMPLETED_BY_CALLEE(52, "被叫结束通话"),

    COMPLETED_BY_GROUP_LEAVE(53, "群聊离开房间"),

    REJECTED(60, "已拒绝"),
    CANCELED(61, "已取消"),
    NO_ANSWER(62, "无应答"),
    DIS_CONNECTION(63, "链接断开")
    ;

    private final Integer value;

    private final String name;

    public static final List<UserChatCallParticipantStatusEnum> NO_ANSWER_STATUS = List.of(NO_ANSWER, CANCELED, DIS_CONNECTION);

    public static final List<UserChatCallParticipantStatusEnum> BUSY_STATUS = List.of(INITIATING, RINGING, CONNECTING, ACTIVE);
}
