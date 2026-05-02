package io.github.loncra.basic.service.message.api.service.feign;

import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.message.api.service.SocketServiceClient;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.socketio.api.metadata.BroadcastMessageMetadata;
import io.github.loncra.framework.socketio.api.metadata.MultipleUnicastMessageMetadata;
import io.github.loncra.framework.socketio.api.metadata.UnicastMessageMetadata;
import io.github.loncra.framework.spring.security.core.authentication.service.feign.FeignAuthenticationConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 消息发送服务的 Feign 到用接口
 *
 * @author maurice
 */
@ConditionalOnClass(FeignClientsConfiguration.class)
@FeignClient(value = SystemConstants.SYS_MESSAGE_NAME, contextId = "socketServiceFeignClient", configuration = FeignAuthenticationConfiguration.class)
public interface SocketServiceFeignClient extends SocketServiceClient {

    /**
     * 广播消息到当前命名空间下的所有连接客户端。
     *
     * @param messageList 广播消息元数据集合
     * @return 发送结果集合，结果数据为每条消息的 {@code data} 字段
     */
    @Override
    @PostMapping("broadcast")
    List<RestResult<?>> broadcast(
            @RequestBody
            List<BroadcastMessageMetadata<?>> messageList
    );

    /**
     * 批量单播消息（一次请求携带多组单播目标与消息体）。
     *
     * @param messageList 批量单播消息元数据集合
     * @return 发送结果集合，结果数据为每条消息的 {@code data} 字段
     */
    @Override
    @PostMapping("multipleUnicast")
    List<RestResult<?>> multipleUnicast(
            @RequestBody
            List<MultipleUnicastMessageMetadata<?>> messageList
    );

    /**
     * 单播消息到指定客户端连接。
     *
     * @param messageList 单播消息元数据集合
     * @return 发送结果集合，结果数据为每条消息的 {@code data} 字段
     */
    @Override
    @PostMapping("unicast")
    List<RestResult<?>> unicast(
            @RequestBody
            List<UnicastMessageMetadata<?>> messageList
    );

    /**
     * 单播消息并等待客户端 ACK 回执。
     *
     * @param messageList 单播消息元数据集合
     * @return 发送结果集合，结果数据为 ACK 回执内容
     */
    @Override
    @PostMapping("unicast/ack")
    List<RestResult<?>> ackUnicast(
            @RequestBody
            List<UnicastMessageMetadata<?>> messageList
    );

    /**
     * 批量单播消息并等待客户端 ACK 回执。
     *
     * @param messageList 批量单播消息元数据集合
     * @return 发送结果集合，结果数据为 ACK 回执内容
     */
    @Override
    @PostMapping("multipleUnicast/ack")
    List<RestResult<?>> ackMultipleUnicast(
            @RequestBody
            List<MultipleUnicastMessageMetadata<?>> messageList
    );

    /**
     * 广播消息并等待客户端 ACK 回执。
     *
     * @param messageList 广播消息元数据集合
     * @return 发送结果集合，结果数据为 ACK 回执内容
     */
    @Override
    @PostMapping("broadcast/ack")
    List<RestResult<?>> ackBroadcast(
            @RequestBody
            List<BroadcastMessageMetadata<?>> messageList
    );

    /**
     * 获取客户端操作系统
     *
     * @param deviceIdentified 设备唯一是被
     * @param typeIdNameMetadata 用户信息
     *
     * @return 客户端连接时的 user-agent 信息
     */
    @Override
    @PostMapping("getClientOperatingSystem/{deviceIdentified}")
    Map<String, Object> getClientOperatingSystem(
            @PathVariable
            String deviceIdentified,
            @RequestBody
            TypeIdNameMetadata typeIdNameMetadata
    );
}
