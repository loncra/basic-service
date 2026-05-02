package io.github.loncra.basic.service.message.server.domain.entity;

import io.github.loncra.basic.service.message.api.enumerate.MessageTypeEnum;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import io.github.loncra.framework.commons.retry.Retryable;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.Instant;

/**
 * 基础消息实体，用于将所有消息内容公有化使用。
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BasicMessageEntity extends LongVersionEntity<Integer> implements Retryable, ExecuteStatus.Body {

    @Serial
    private static final long serialVersionUID = -1167940666968537341L;

    public static final String TO_PRINCIPAL_METADATA_KEY = "toPrincipal";

    /**
     * 类型
     *
     * @see MessageTypeEnum
     */
    private MessageTypeEnum type;

    /**
     * 内容
     */
    private String content;


    /**
     * 重试次数
     */
    private Integer retryCount = 1;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 3;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * 发送成功时间
     */
    private Instant successTime;

    /**
     * 状态：0.执行中、1.执行成功，2.重试中，99.执行失败
     *
     * @see ExecuteStatus
     */
    private ExecuteStatus executeStatus = ExecuteStatus.Processing;

    /**
     * 重试时间
     */
    private Instant retryTime;

    /**
     * 备注
     */
    private String remark;

}
