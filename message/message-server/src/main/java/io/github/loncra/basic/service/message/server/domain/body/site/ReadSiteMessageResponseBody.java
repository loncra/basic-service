package io.github.loncra.basic.service.message.server.domain.body.site;

import io.github.loncra.basic.service.message.server.domain.entity.SiteMessageEntity;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReadSiteMessageResponseBody extends SiteMessageEntity {

    @Serial
    private static final long serialVersionUID = 1958479800640562304L;

    private YesOrNo beforeReadable;
}
