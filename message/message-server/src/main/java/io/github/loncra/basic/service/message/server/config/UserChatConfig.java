package io.github.loncra.basic.service.message.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loncra.basic-service.message.app.chat")
public class UserChatConfig {

    private String joinRoomText = "{0} 将 {1}, 加入了群";

    private String updateParticipantTypeText = "{0} 将 {1}, 设置成为了{2}";

    private String roomRemoveParticipant = "{0} 将 {1}, 在本群中移除";

    private String roomRenameText = "{0} 将群名称改成了 {1}";

    private String existRoomText = "{0} 退出了群聊";

    private String disbandRoomText = "管理员 {0} 解散了群聊";

    private String ownerChangeText = "{0} 成为了群主";

    private Integer conversationCoverLimit = 3;

    private Integer conversationNameLimit = 3;
}
