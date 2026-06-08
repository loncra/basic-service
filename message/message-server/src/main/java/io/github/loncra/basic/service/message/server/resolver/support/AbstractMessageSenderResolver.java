package io.github.loncra.basic.service.message.server.resolver.support;

import io.github.loncra.basic.service.message.server.domain.entity.BasicMessageEntity;
import io.github.loncra.basic.service.message.server.enumerate.UnreadQuantityGroupEnum;
import io.github.loncra.basic.service.message.server.resolver.MessageSenderResolver;
import io.github.loncra.basic.service.message.server.resolver.UnreadQuantityMessageResolver;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 抽象的消息发送者实现，主要是构建和验证发送消息实体的一个抽象类
 *
 * @param <T> 消息的请求数据泛型实体类型
 * @author maurice
 */
@Slf4j
public abstract class AbstractMessageSenderResolver<T extends BasicMessageEntity> implements MessageSenderResolver {

    public static final String DEFAULT_BATCH_MESSAGE_KEY = "messages";

    @Setter
    private Validator validator;

    /**
     * 请求对象的数据实体类型
     */
    protected final Class<T> entityClass;

    public AbstractMessageSenderResolver() {
        this.entityClass = getGenericClass(getClass(), 0);
    }

    @Override
    public RestResult<Object> send(Map<String, Object> request) throws Exception {

        List<T> result = new LinkedList<>();

        // 如果存在批量消息构造数据集合，否则构造单个实体
        if (request.containsKey(DEFAULT_BATCH_MESSAGE_KEY)) {
            //noinspection unchecked
            List<Map<String, Object>> messages = CastUtils.cast(request.get(DEFAULT_BATCH_MESSAGE_KEY), List.class);

            for (Map<String, Object> m : messages) {
                T entity = bindAndValidate(m);
                result.add(entity);
            }

        } else {
            T entity = bindAndValidate(request);
            result.add(entity);
        }

        return sendMessage(result);
    }

    /**
     * 发送消息
     *
     * @param result body 集合
     * @return reset 结果集
     */
    public abstract RestResult<Object> sendMessage(List<T> result);

    /**
     * 绑定并验证请求数据
     *
     * @param value 请求参数
     * @return body
     * @throws BindException 验证数据错误时抛出
     */
    private T bindAndValidate(Map<String, Object> value) throws BindException {
        T entity = CastUtils.convertValue(value, entityClass);
        WebDataBinder binder = new WebDataBinder(entity, entity.getClass().getSimpleName());

        if (validator != null) {

            binder.setValidator(validator);
            binder.validate();

            if (binder.getBindingResult().hasErrors()) {
                throw new BindException(binder.getBindingResult());
            }

        }

        postBindValue(entity, value);

        return entity;
    }

    /**
     * 绑定值后的处理
     *
     * @param entity 消息的请求数据泛型实体
     * @param value  被绑定的数据值（请求参数）
     */
    protected void postBindValue(T entity, Map<String, Object> value) {

    }

}
