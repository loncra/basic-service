package io.github.loncra.basic.service.gateway;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.loncra.basic.service.gateway.config.ApplicationConfig;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.exception.ErrorCodeException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * rest result 形式的 sentinel 异常响应
 *
 * @author maurice.chen
 */
@RequiredArgsConstructor
public class RestResultGatewayBlockExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    private final ApplicationConfig applicationConfig;

    @Override
    public @NonNull Mono<Void> handle(
            ServerWebExchange exchange,
            Throwable ex
    ) {

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        if (!BlockException.isBlockException(ex)) {
            return Mono.error(ex);
        }

        return exchange.getResponse()
                       .writeWith(Mono.create(dataBuffer -> formatResponse(exchange, dataBuffer)));

    }

    private void formatResponse(
            ServerWebExchange exchange,
            MonoSink<DataBuffer> dataBuffer
    ) {
        exchange.getResponse()
                .setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        exchange.getResponse()
                .getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        String message = applicationConfig.getDefaultReasonPhrase();
        int statusValue = HttpStatus.INTERNAL_SERVER_ERROR.value();

        ServerHttpResponse response = exchange.getResponse();
        if (Objects.nonNull(response.getStatusCode())) {
            HttpStatus status = HttpStatus.valueOf(response.getStatusCode().value());
            message = status.getReasonPhrase();
            statusValue = status.value();
        }

        RestResult<Object> result = RestResult.of(
                message,
                statusValue,
                ErrorCodeException.DEFAULT_EXCEPTION_CODE,
                new LinkedHashMap<>()
        );

        byte[] bytes;

        try {
            bytes = objectMapper.writeValueAsBytes(result);
        } catch (Exception e) {
            bytes = e.getMessage().getBytes();
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        dataBuffer.success(buffer);
    }

}
