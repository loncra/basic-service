package io.github.loncra.basic.service.commons.domain.metadata;

import io.github.loncra.basic.service.commons.enumerate.AuditStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class AuditMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 2776168057730503391L;

    private AuditStatusEnum status;

    private String remark;
}
