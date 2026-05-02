package io.github.loncra.basic.service.message.server.domain.body.sms;

import io.github.loncra.basic.service.commons.enumerate.AuditStatusEnum;
import io.github.loncra.basic.service.message.api.domian.metadata.AlibabaCloudAuditMetadata;
import io.github.loncra.basic.service.message.api.domian.metadata.SmsSignMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class SmsSignResponseBody extends SmsSignMetadata implements AlibabaCloudAuditMetadata {

    @Serial
    private static final long serialVersionUID = 9002614061162381681L;
    /**
     * 审核状态
     */
    private AuditStatusEnum status;

    /**
     * 审核时间
     */
    private Instant auditionTime;
}
