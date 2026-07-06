package io.github.loncra.basic.service.message.server.controller;

import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallEntity;
import io.github.loncra.basic.service.message.server.service.chat.call.UserChatCallManager;
import io.livekit.server.WebhookReceiver;
import livekit.LivekitWebhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/live/kit/webhooks")
public class LiveKitMediaServerWebhookController {

    private final UserChatCallManager userChatCallManager;

    @PostMapping
    public void handleLiveKitWebhook(
            @RequestHeader("Authorization") String authHeader,// 拿到加密签名头部
            @RequestBody String body// 拿到原始报文
    ) {
        WebhookReceiver receiver = new WebhookReceiver(
                userChatCallManager.getUserChatCallConfig().getLivekit().getSecret().getSecretId(),
                userChatCallManager.getUserChatCallConfig().getLivekit().getSecret().getSecretKey()
        );
        try {
            // 1. 验证签名并解析成事件对象（防止黑客伪造请求）
            LivekitWebhook.WebhookEvent event = receiver.receive(body, authHeader);

            // 2. 识别事件类型
            String eventName = event.getEvent();
            var roomInfo = event.getRoom(); // 获取当前事件关联的房间信息

            if (log.isDebugEnabled()) {
                log.debug("收到 LiveKit 事件: {}, 房间名: {}", eventName, roomInfo.getName());
            }

            // 3. 根据不同的事件写你的业务逻辑
            switch (eventName) {
                case "room_finished":
                    Long id = NumberUtils.toLong(StringUtils.substringAfter(roomInfo.getName(), UserChatCallEntity.ROOM_ID_PREFIX));
                    userChatCallManager.timeout(id);
                    break;

                case "participant_left":
                    // 用户挂断离开
                    System.out.println("用户 " + event.getParticipant().getIdentity() + " 离开了房间");
                    break;
            }

        } catch (Exception e) {
            log.warn("liveKit web hook 出现异常", e);
        }
    }
}
