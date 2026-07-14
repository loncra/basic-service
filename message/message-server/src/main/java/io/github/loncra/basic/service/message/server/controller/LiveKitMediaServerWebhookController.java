package io.github.loncra.basic.service.message.server.controller;

import io.github.loncra.basic.service.message.server.constants.LiveKitWebhookEventConstants;
import io.github.loncra.basic.service.message.server.event.LiveKitMediaServerWebhookEventListener;
import io.github.loncra.basic.service.message.server.service.chat.call.UserChatCallManager;
import io.livekit.server.WebhookReceiver;
import livekit.LivekitModels;
import livekit.LivekitWebhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/live/kit/webhooks")
public class LiveKitMediaServerWebhookController {

    private final UserChatCallManager userChatCallManager;

    private final ApplicationEventPublisher applicationEventPublisher;

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
            // 获取当前事件关联的房间信息
            LivekitModels.Room roomInfo = event.getRoom();
            if (LiveKitWebhookEventConstants.ROOM_FINISHED.equals(eventName)) {
                userChatCallManager.timeout(LiveKitMediaServerWebhookEventListener.getUserChatCallId(roomInfo.getName()));
            } else {
                applicationEventPublisher.publishEvent(event);
            }

        } catch (Exception e) {
            log.warn("liveKit web hook 出现异常", e);
        }
    }
}
