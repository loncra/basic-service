package io.github.loncra.basic.service.message.server.controller;

import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.socketio.api.SocketPrincipal;
import io.github.loncra.framework.socketio.api.metadata.BroadcastMessageMetadata;
import io.github.loncra.framework.socketio.api.metadata.MultipleUnicastMessageMetadata;
import io.github.loncra.framework.socketio.api.metadata.UnicastMessageMetadata;
import io.github.loncra.framework.socketio.core.SocketServerManager;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AuditAuthenticationSuccessDetails;
import io.github.loncra.framework.spring.web.device.DeviceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Socket 消息控制器，提供广播、单播及带 ACK 的消息下发能力。
 *
 * 该控制器仅在 {@code loncra.framework.socketio.enabled=true}（或未配置）时生效。
 * 除健康检查外，其余接口均要求调用方具备 {@code FEIGN} 角色。
 *
 * @author maurice.chen
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "loncra.framework.socketio", name = "enabled", matchIfMissing = true)
public class SocketMessageController {

    private final SocketServerManager socketServerManager;

    /**
     * 获取 Socket 服务健康状态。
     *
     * @return REST 响应结果
     */
    @GetMapping("health")
    public RestResult<Health> health() {
        return RestResult.ofSuccess(Health.up().build());
    }

    /**
     * 广播消息到当前命名空间下的所有连接客户端。
     *
     * @param messageList 广播消息元数据集合
     * @return REST 响应结果
     */
    @PostMapping("broadcast")
    public List<RestResult<?>> broadcast(
            @RequestBody
            List<BroadcastMessageMetadata<?>> messageList
    ) {
        if (log.isDebugEnabled()) {
            log.debug("broadcast messageList: {}", SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(messageList)));
        }
        return messageList
                .stream()
                .peek(socketServerManager::sendMessage)
                .map(r -> RestResult.ofSuccess(r.getMessage().getData()))
                .collect(Collectors.toList());
    }

    /**
     * 批量单播消息（一次请求携带多组单播目标与消息体）。
     *
     * @param messageList 批量单播消息元数据集合
     * @return 发送结果集合
     */
    @PostMapping("unicast/multiple")
    public List<RestResult<?>> multipleUnicast(
            @RequestBody
            List<MultipleUnicastMessageMetadata<?>> messageList
    ) {

        messageList
                .stream()
                .flatMap(r -> r.toUnicastMessageList().stream())
                .forEach(socketServerManager::sendMessage);

        return messageList
                .stream()
                .map(r -> RestResult.ofSuccess(r.getMessage().getData()))
                .collect(Collectors.toList());
    }

    /**
     * 单播消息到指定客户端连接。
     *
     * @param messageList 单播消息元数据集合
     * @return 发送结果集合
     */
    @PostMapping("unicast")
    public List<RestResult<?>> unicast(
            @RequestBody
            List<UnicastMessageMetadata<?>> messageList
    ) {
        return messageList.stream()
                .peek(socketServerManager::sendMessage)
                .map(r -> RestResult.ofSuccess(r.getMessage().getData()))
                .collect(Collectors.toList());
    }

    /**
     * 单播消息并等待客户端 ACK 回执。
     *
     * @param messageList 单播消息元数据集合
     * @return ACK 回执结果集合
     */
    @PostMapping("unicast/ack")
    public List<RestResult<?>> ackUnicast(
            @RequestBody
            List<UnicastMessageMetadata<?>> messageList
    ) throws ExecutionException, InterruptedException {
        List<RestResult<?>> results = new LinkedList<>();
        for (UnicastMessageMetadata<?> message : messageList) {
            Object r = socketServerManager.ackSendMessage(message);
            results.add(RestResult.ofSuccess(r));
        }

        return results;

    }

    /**
     * 批量单播消息并等待客户端 ACK 回执。
     *
     * @param messageList 批量单播消息元数据集合
     * @return ACK 回执结果集合
     */
    @PostMapping("unicast/multiple/ack")
    public List<RestResult<?>> ackMultipleUnicast(
            @RequestBody
            List<MultipleUnicastMessageMetadata<?>> messageList
    ) throws ExecutionException, InterruptedException {
        List<RestResult<?>> results = new LinkedList<>();
        for (MultipleUnicastMessageMetadata<?> message : messageList) {
            Object r = socketServerManager.ackSendMessage(message);
            results.add(RestResult.ofSuccess(r));
        }

        return results;

    }

    /**
     * 广播消息并等待客户端 ACK 回执。
     *
     * @param messageList 广播消息元数据集合
     * @return ACK 回执结果集合
     */
    @PostMapping("broadcast/ack")
    public List<RestResult<?>> ackBroadcast(
            @RequestBody
            List<BroadcastMessageMetadata<?>> messageList
    ) throws ExecutionException, InterruptedException {
        List<RestResult<?>> results = new LinkedList<>();
        for (BroadcastMessageMetadata<?> message : messageList) {
            Object r = socketServerManager.ackSendMessage(message);
            results.add(RestResult.ofSuccess(r));
        }
        return results;
    }

    /**
     * 获取客户端操作系统
     *
     * @param deviceIdentified 设备唯一是被
     * @param typeIdNameMetadata 用户信息
     *
     * @return REST 响应结果
     */
    @PostMapping("clientOperatingSystem/{deviceIdentified}")
    public RestResult<Map<String, Object>> getClientOperatingSystem(
            @PathVariable
            String deviceIdentified,
            @RequestBody
            TypeIdNameMetadata typeIdNameMetadata
    ) {

        SecurityContext securityContext = socketServerManager.getAccessTokenContextRepository()
                .getSecurityContext(typeIdNameMetadata.getType(), typeIdNameMetadata.getId());

        SystemException.isTrue(Objects.nonNull(securityContext), "找不到租户为 [" + typeIdNameMetadata.toPrincipalName() + "] 的认证信息");
        SystemException.isTrue(Objects.nonNull(securityContext.getAuthentication()), "找不到租户为 [" + typeIdNameMetadata.toPrincipalName() + "] 的认证信息");

        AuditAuthenticationToken auditAuthenticationToken = CastUtils.cast(securityContext.getAuthentication());
        AuditAuthenticationSuccessDetails details = CastUtils.cast(auditAuthenticationToken.getDetails());

        Object clientIdsObject = details.getMetadata().get(SocketPrincipal.DEFAULT_SOCKET_CLIENT_ID_NAME);
        SystemException.isTrue(Objects.nonNull(clientIdsObject), "找不到租户为 [" + typeIdNameMetadata.toPrincipalName() + "] 的认证客户端设备");

        Set<String> clientIds = CastUtils.cast(clientIdsObject);

        SystemException.isTrue(clientIds.contains(deviceIdentified), "设备 [" + deviceIdentified + "] 不在当前用户认证信息里，可能已经关闭了应用");

        AuditAuthenticationToken socketToken = socketServerManager.getSocketAuditAuthenticationToken(deviceIdentified);
        AuditAuthenticationSuccessDetails socketSuccessDetails = CastUtils.cast(socketToken.getDetails());
        String userAgent = Objects.toString(socketSuccessDetails.getMetadata().get(DeviceUtils.USER_AGENT_HEADER_NAME.toLowerCase()));

        SystemException.isTrue(StringUtils.isNotEmpty(userAgent), "找不到连接后的" + DeviceUtils.USER_AGENT_HEADER_NAME + "信息");

        UserAgent userAgentDevice = DeviceUtils.getDevice(userAgent);

        return RestResult.ofSuccess(Map.of(
                UserAgent.OPERATING_SYSTEM_CLASS, userAgentDevice.get(UserAgent.OPERATING_SYSTEM_CLASS),
                UserAgent.OPERATING_SYSTEM_NAME, userAgentDevice.get(UserAgent.OPERATING_SYSTEM_NAME),
                UserAgent.DEVICE_CPU, userAgentDevice.get(UserAgent.DEVICE_CPU),
                UserAgent.DEVICE_CPU_BITS, userAgentDevice.get(UserAgent.DEVICE_CPU_BITS),
                UserAgent.AGENT_CLASS, userAgentDevice.get(UserAgent.AGENT_CLASS),
                UserAgent.AGENT_NAME, userAgentDevice.get(UserAgent.AGENT_NAME),
                UserAgent.AGENT_VERSION, userAgentDevice.get(UserAgent.AGENT_NAME)
        ));
    }
}