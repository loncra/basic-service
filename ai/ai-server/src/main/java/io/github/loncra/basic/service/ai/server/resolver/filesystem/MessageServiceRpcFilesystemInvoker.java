package io.github.loncra.basic.service.ai.server.resolver.filesystem;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.loncra.basic.service.ai.server.enumerate.RpcFilesystemOperationEnum;
import io.github.loncra.basic.service.message.api.service.SocketServiceClient;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.socketio.api.metadata.UnicastMessageMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageServiceRpcFilesystemInvoker implements RpcFilesystemInvoker {

    public static final String REQUEST_EVENT_NAME = "agent_filesystem_request";

    private static final String OPERATION_FIELD_NAME = "operation";

    private static final String PARAMS_FIELD_NAME = "params";

    private static final String ACK_MESSAGE_FIELD_NAME = "message";

    private static final String ACK_VALUE_FIELD_NAME = "value";

    private static final int REQUEST_TIMEOUT_SECONDS = 60;

    private static final int FAIL_STATUS = 500;

    private final SocketServiceClient socketServiceClient;

    @Override
    public RestResult<Object> invoke(
            String deviceIdentified,
            RpcFilesystemOperationEnum operation,
            Map<String, Object> params
    ) {

        if (StringUtils.isEmpty(deviceIdentified)) {
            return RestResult.of("当前会话没有绑定客户端设备，无法执行文件系统操作", FAIL_STATUS);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put(OPERATION_FIELD_NAME, operation.getValue());
        payload.put(PARAMS_FIELD_NAME, params);

        UnicastMessageMetadata<Map<String, Object>> metadata = UnicastMessageMetadata.of(
                deviceIdentified,
                REQUEST_EVENT_NAME,
                RestResult.ofSuccess(payload)
        );

        metadata.getMetadata().put(TimeProperties.class.getName(), TimeProperties.ofSeconds(REQUEST_TIMEOUT_SECONDS));

        try {
            List<RestResult<?>> results = socketServiceClient.ackUnicast(List.of(metadata));

            if (CollectionUtils.isEmpty(results)) {
                return RestResult.of("客户端 [" + deviceIdentified + "] 无应答", FAIL_STATUS);
            }

            Object data = results.getFirst().getData();

            if (Objects.isNull(data)) {
                return RestResult.of("客户端 [" + deviceIdentified + "] 无应答", FAIL_STATUS);
            }

            Map<String, Object> ack = CastUtils.convertValue(data, CastUtils.MAP_TYPE_REFERENCE);
            Object message = ack.get(ACK_MESSAGE_FIELD_NAME);
            Object value = message instanceof Map<?, ?> map ? map.get(ACK_VALUE_FIELD_NAME) : null;

            if (Objects.isNull(value)) {
                return RestResult.of("客户端 [" + deviceIdentified + "] 响应超时", FAIL_STATUS);
            }

            RestResult<Object> response = CastUtils.convertValue(
                    value,
                    new TypeReference<>() {}
            );

            return Objects.isNull(response)
                    ? RestResult.of("客户端 [" + deviceIdentified + "] 返回内容为空", FAIL_STATUS)
                    : response;
        }
        catch (Exception e) {
            log.warn("客户端文件系统请求失败, deviceIdentified={}, operation={}", deviceIdentified, operation, e);
            return RestResult.of("客户端文件系统请求失败: " + e.getMessage(), FAIL_STATUS);
        }

    }

}
