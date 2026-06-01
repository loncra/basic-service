package io.github.loncra.basic.service.message.server.resolver.support;

import io.github.loncra.basic.service.message.server.domain.entity.BasicMessageEntity;
import io.github.loncra.basic.service.message.server.domain.entity.BatchMessageEntity;
import io.github.loncra.basic.service.message.server.enumerate.BatchMessageTypeEnum;
import io.github.loncra.basic.service.message.server.service.BatchMessageService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.retry.Retryable;
import io.github.loncra.framework.idempotent.ConcurrentConfig;
import io.github.loncra.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 批量消息发送的抽象实现，用于对需要创建 tb_batch_message 记录的消息做一个统一处理
 *
 * @author maurice.chen
 *
 * @param <T> 批量消息数据的泛型实体类型
 * @param <S> 请求的消息数据泛型实体类型
 */
@Slf4j
@Setter(onMethod_ = @Autowired)
public abstract class AbstractBatchMessageSenderResolver<T extends BasicMessageEntity, S extends BatchMessageEntity.Body> extends AbstractMessageSenderResolver<T> {

    public static final String DEFAULT_MESSAGE_COUNT_KEY = "count";

    public static final String DEFAULT_BATCH_MESSAGE_ID_KEY = "batchId";

    public static final String BATCH_UPDATE_CONCURRENT_KEY = "loncra:basic-service:message:concurrent:batch-update:";

    protected final Class<S> sendEntityClass;

    /**
     * 批量消息无服务
     */
    private BatchMessageService batchMessageService;

    /**
     * 线程池，用于批量发送消息时候异步使用。
     */
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private ConcurrentInterceptor concurrentInterceptor;

    public AbstractBatchMessageSenderResolver() {
        this.sendEntityClass = getGenericClass(getClass(), 1);
    }


    @Override
    public RestResult<Object> sendMessage(List<T> result) {

        List<S> content = getBatchMessageBodyContent(result);

        Objects.requireNonNull(content, "批量消息 body 内容不能为空");

        RestResult<Object> restResult = RestResult.ofException(
                String.valueOf(HttpStatus.NO_CONTENT.value()),
                new SystemException("未知执行结果")
        );

        content
                .stream()
                .filter(c -> Retryable.class.isAssignableFrom(c.getClass()))
                .map(c -> CastUtils.cast(c, Retryable.class))
                .forEach(c -> c.setMaxRetryCount(getMaxRetryCount()));

        if (content.size() > 1) {

            BatchMessageEntity batchMessage = new BatchMessageEntity();

            batchMessage.setCount(content.size());

            BatchMessageTypeEnum type = BatchMessageTypeEnum.valueOf(entityClass);
            batchMessage.setType(type);

            batchMessageService.save(batchMessage);

            content.forEach(r -> r.setBatchId(batchMessage.getId()));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put(DEFAULT_BATCH_MESSAGE_ID_KEY, batchMessage.getId());
            data.put(DEFAULT_MESSAGE_COUNT_KEY, content.size());

            threadPoolTaskExecutor.execute(() -> doSend(content));

            restResult = RestResult.ofSuccess(
                    "发送" + content.size() + "条 [" + getMessageType() + "] 消息成功",
                    data
            );
        } else {
            RestResult<Object> sendResult = doSend(content);
            if (Objects.nonNull(sendResult)) {
                restResult = sendResult;
            }
        }

        return restResult;
    }

    public RestResult<Object> doSend(List<S> content) {
        List<S> newData = preSend(content);

        if (CollectionUtils.isNotEmpty(newData)) {
            return send(newData);
        }

        return null;
    }

    /**
     * 发送消息前的处理
     *
     * @param content 批量消息内容
     * @return true 继续发送，否则 false
     */
    public List<S> preSend(List<S> content) {
        return content;
    }

    /**
     * 发送批量消息
     *
     * @param result 批量消息内容
     * @return rest 结果集
     */
    public abstract RestResult<Object> send(List<S> result);

    /**
     * 获取批量消息数据内容
     *
     * @param result 消息的请求数据泛型实体集合
     * @return 批量消息数据的泛型实体集合
     */
    protected abstract List<S> getBatchMessageBodyContent(List<T> result);

    /**
     * 更新批量消息
     *
     * @param body 批量消息接口实现类
     */
    public void syncBatchMessage(BatchMessageEntity.Body body) {

        ConcurrentConfig properties = new ConcurrentConfig();
        properties.setKey(BATCH_UPDATE_CONCURRENT_KEY + body.getBatchId());
        properties.setWaitTime(TimeProperties.ofSeconds(3));
        properties.setLeaseTime(TimeProperties.ofSeconds(5));

        concurrentInterceptor.invoke(properties, () -> updateBatchMessage(body));
    }

    private void updateBatchMessage(BatchMessageEntity.Body body) {
        BatchMessageEntity batchMessage = batchMessageService.get(body.getBatchId());

        if (ExecuteStatus.Success.equals(body.getExecuteStatus())) {
            batchMessage.setSuccessNumber(batchMessage.getSuccessNumber() + 1);
        } else if (ExecuteStatus.Failure.equals(body.getExecuteStatus())) {
            batchMessage.setFailNumber(batchMessage.getFailNumber() + 1);
        }

        if (batchMessage.getCount().equals(batchMessage.getSuccessNumber() + batchMessage.getFailNumber())) {
            batchMessage.setExecuteStatus(ExecuteStatus.Success);
            batchMessage.setCompleteTime(Instant.now());

            onBatchMessageComplete(batchMessage);
        }

        batchMessageService.save(batchMessage);
    }

    /**
     * 当批量信息发送完成时，触发此方法。
     *
     * @param batchMessage 批量信息实体
     */
    protected void onBatchMessageComplete(BatchMessageEntity batchMessage) {
    }

    protected void sendMessage(Long id) {
        S entity = doSendMessage(id);

        if (Objects.isNull(entity)) {
            log.warn("通过实现类:{} 找不到 ID 为: {} 的数据内容", this.getClass().getSimpleName(), id);
            return;
        }

        if (entity instanceof ExecuteStatus.Body body && ExecuteStatus.Failure.equals(body.getExecuteStatus())) {
            log.warn("发送 [{}] 消息失败,原因:{}", getMessageType(), body.getException());
        }

        sendMessageComplete(entity);

    }

    /**
     * 发送完成后触发此方法
     *
     * @param entity 消息实体
     */
    public void sendMessageComplete(S entity) {

    }

    /**
     * 执行发送消息
     *
     * @param id 消息 id
     *
     * @return 执行成功后的消息实体
     */
    public abstract S doSendMessage(Long id);

    /**
     * 获取最大重试次数
     *
     * @return 重试次数
     */
    protected int getMaxRetryCount() {
        return 0;
    }

    @Autowired
    public void setBatchMessageService(BatchMessageService batchMessageService) {
        this.batchMessageService = batchMessageService;
    }

}
