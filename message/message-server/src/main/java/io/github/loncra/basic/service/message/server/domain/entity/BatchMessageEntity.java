package io.github.loncra.basic.service.message.server.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.message.server.enumerate.BatchMessageTypeEnum;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import io.github.loncra.framework.commons.id.BasicIdentification;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;


/**
 * <p>批量消息实体类</p>
 * <p>Table: tb_batch_message - 批量消息</p>
 *
 * @author maurice
 * @since 2021-08-22 04:45:14
 */
@Data
@NoArgsConstructor
@Alias("batchMessage")
@TableName("tb_batch_message")
@EqualsAndHashCode(callSuper = true)
public class BatchMessageEntity extends LongVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = 3580346090724641812L;

    /**
     * 完成时间
     */
    private Instant completeTime;

    /**
     * 状态:0.执行中、1.执行成功，99.执行失败
     */
    private ExecuteStatus executeStatus = ExecuteStatus.Processing;

    /**
     * 总数
     */
    private Integer count = 0;

    /**
     * 成功发送数量
     */
    private Integer successNumber = 0;

    /**
     * 失败发送数量
     */
    private Integer failNumber = 0;

    /**
     * 类型:10.站内信,20.邮件,30.短信
     */
    private BatchMessageTypeEnum type;

    /**
     * 批量消息接口，用于统一规范使用
     *
     * @author maurice.chen
     */
    public interface Body extends BasicIdentification<Long> {

        /**
         * 获取批量消息 id
         *
         * @return 批量消息 id
         */
        Long getBatchId();

        /**
         * 设置批量消息 id
         *
         * @param batchId 批量消息 id
         */
        void setBatchId(Long batchId);

        /**
         * 获取状态
         *
         * @return 执行状态
         */
        ExecuteStatus getExecuteStatus();
    }

}