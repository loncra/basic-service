package io.github.loncra.basic.service.message.api.service.web;

import io.github.loncra.basic.service.message.api.service.MessageServiceClient;
import io.github.loncra.framework.commons.RestResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Map;

/**
 * 消息发送服务的 Feign 到用接口
 *
 * @author maurice
 */
public interface MessageServiceWebClient extends MessageServiceClient {

    /**
     * 发送消息
     *
     * @param request 请求参数
     * @return rest 结果集
     */
    @Override
    @PostExchange("send")
    RestResult<Object> send(@RequestBody Map<String, Object> request);

}
