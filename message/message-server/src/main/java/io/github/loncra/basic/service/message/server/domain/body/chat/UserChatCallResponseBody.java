package io.github.loncra.basic.service.message.server.domain.body.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatRoomEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallParticipantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties("userChatRoomId")
public class UserChatCallResponseBody extends UserChatCallEntity {

    private UserChatRoomEntity room;

    private List<UserChatCallParticipantEntity> participants = new LinkedList<>();
}
