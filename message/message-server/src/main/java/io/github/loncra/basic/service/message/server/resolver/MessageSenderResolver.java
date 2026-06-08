package io.github.loncra.basic.service.message.server.resolver;

import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import org.springframework.core.ResolvableType;

import java.util.Map;

/**
 * 消息发送者
 *
 * @author maurice
 */
public interface MessageSenderResolver {

    /**
     * 发送消息
     *
     * @param request http servlet request
     * @return rest 结果集
     * @throws Exception 发送错误时抛出
     */
    RestResult<Object> send(Map<String, Object> request) throws Exception;

    /**
     * 获取类型
     *
     * @return 类型
     */
    String getMessageType();

    /**
     * 获取泛型类型
     *
     * @param targetClass 目标类
     * @param index       泛型索引位置
     * @param <T>         泛型类型
     *
     * @return 泛型类型
     */
    @SuppressWarnings("unchecked")
    default <T> Class<T> getGenericClass(
            Class<?> targetClass,
            int index
    ) {
        ResolvableType resolvableType = ResolvableType.forClass(targetClass);
        ResolvableType superType = resolvableType.getSuperType();

        if (superType == ResolvableType.NONE) {
            return (Class<T>) Object.class;
        }

        ResolvableType genericType = superType.getGeneric(index);
        Class<?> resolved = genericType.resolve();

        if (resolved == null) {
            return (Class<T>) Object.class;
        }

        return (Class<T>) resolved;
    }
}
