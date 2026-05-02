package io.github.loncra.basic.service.message.server.domain.body.sms;

import io.github.loncra.basic.service.commons.enumerate.AuditStatusEnum;
import io.github.loncra.basic.service.message.api.domian.metadata.AlibabaCloudAuditMetadata;
import io.github.loncra.basic.service.message.api.domian.metadata.SmsTemplateMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class SmsTemplateResponseBody extends SmsTemplateMetadata implements AlibabaCloudAuditMetadata {
    @Serial
    private static final long serialVersionUID = 8756695231119888411L;
    /**
     * 审核状态
     */
    private AuditStatusEnum status;

    /**
     * 审核时间
     */
    private Instant auditionTime;
}
