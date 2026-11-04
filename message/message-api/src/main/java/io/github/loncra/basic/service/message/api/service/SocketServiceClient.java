package io.github.loncra.basic.service.message.api.service;

import io.github.loncra.basic.service.message.api.domian.metadata.SocketSessionMetadata;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.socketio.api.metadata.BroadcastMessageMetadata;
import io.github.loncra.framework.socketio.api.metadata.MultipleUnicastMessageMetadata;
import io.github.loncra.framework.socketio.api.metadata.UnicastMessageMetadata;

import java.util.List;

/**
 * 消息发送服务的 Feign 到用接口
 *
 * @author maurice
 */
public interface SocketServiceClient {

    /**
     * 广播消息到当前命名空间下的所有连接客户端。
     *
     * @param messageList 广播消息元数据集合
     * @return 发送结果集合，结果数据为每条消息的 {@code data} 字段
     */
    List<RestResult<?>> broadcast(
            List<BroadcastMessageMetadata<?>> messageList
    );

    /**
     * 批量单播消息（一次请求携带多组单播目标与消息体）。
     *
     * @param messageList 批量单播消息元数据集合
     * @return 发送结果集合，结果数据为每条消息的 {@code data} 字段
     */
    List<RestResult<?>> multipleUnicast(
            List<MultipleUnicastMessageMetadata<?>> messageList
    );

    /**
     * 单播消息到指定客户端连接。
     *
     * @param messageList 单播消息元数据集合
     * @return 发送结果集合，结果数据为每条消息的 {@code data} 字段
     */
    List<RestResult<?>> unicast(
            List<UnicastMessageMetadata<?>> messageList
    );

    /**
     * 单播消息并等待客户端 ACK 回执。
     *
     * @param messageList 单播消息元数据集合
     *
     * @return 发送结果集合，结果数据为 ACK 回执内容
     */
    List<RestResult<?>> ackUnicast(
            List<UnicastMessageMetadata<?>> messageList
    );

    /**
     * 批量单播消息并等待客户端 ACK 回执。
     *
     * @param messageList 批量单播消息元数据集合
     * @return 发送结果集合，结果数据为 ACK 回执内容
     */
    List<RestResult<?>> ackMultipleUnicast(
            List<MultipleUnicastMessageMetadata<?>> messageList
    );

    /**
     * 广播消息并等待客户端 ACK 回执。
     *
     * @param messageList 广播消息元数据集合
     * @return 发送结果集合，结果数据为 ACK 回执内容
     */
    List<RestResult<?>> ackBroadcast(
            List<BroadcastMessageMetadata<?>> messageList
    );

    /**
     * 查询 socket 会话在线状态与客户端系统信息
     *
     * @param deviceIdentified 设备唯一识别
     * @return 会话元数据，离线时 online 为 false
     */
    SocketSessionMetadata getSocketSession(String deviceIdentified);
}
