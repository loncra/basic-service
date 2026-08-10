package io.github.loncra.basic.service.ai.server.domain.body;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 澄清表单提交请求（独立于 {@link AgentResumeRequestBody}）
 */
@Data
public class AgentClarifyRequestBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private Long assistantMessageId;

    /**
     * 对应 clarify_exit 的 toolCallId
     */
    @NotBlank
    private String toolCallId;

    /**
     * 有有效表单值 = 提交并退出澄清；缺省 / null / 空 Map = 取消澄清
     */
    private Map<String, Object> answers;

    /**
     * 可选人读摘要
     */
    private String summary;
}
