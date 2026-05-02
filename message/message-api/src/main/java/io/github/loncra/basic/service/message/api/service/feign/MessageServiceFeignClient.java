package io.github.loncra.basic.service.message.api.service.feign;

import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.message.api.service.MessageServiceClient;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.spring.security.core.authentication.service.feign.FeignAuthenticationConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 消息发送服务的 Feign 到用接口
 *
 * @author maurice
 */
@ConditionalOnClass(FeignClientsConfiguration.class)
@FeignClient(value = SystemConstants.SYS_MESSAGE_NAME, contextId = "messageServiceFeignClient", configuration = FeignAuthenticationConfiguration.class)
public interface MessageServiceFeignClient extends MessageServiceClient {

    /**
     * 发送消息
     *
     * @param request 请求参数
     * @return rest 结果集
     */
    @Override
    @PostMapping("send")
    RestResult<Object> send(@RequestBody Map<String, Object> request);

}
