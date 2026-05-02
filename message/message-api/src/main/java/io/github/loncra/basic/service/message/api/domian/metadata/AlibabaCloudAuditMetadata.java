package io.github.loncra.basic.service.message.api.domian.metadata;

import io.github.loncra.basic.service.commons.enumerate.AuditStatusEnum;

import java.time.Instant;
import java.util.Map;

public interface AlibabaCloudAuditMetadata {

    String INFO_FIELD_KEY = "auditInfo";

    String REJECT_INFO_KEY = "rejectInfo";

    String REJECT_SUB_INFO_KEY = "rejectSubInfo";

    String REJECT_DATE_KEY = "rejectDate";

    AuditStatusEnum getStatus();

    void setStatus(AuditStatusEnum auditStatus);

    Instant getAuditionTime();

    void setAuditionTime(Instant auditionTime);

    Map<String, Object> getMetadata();
}
