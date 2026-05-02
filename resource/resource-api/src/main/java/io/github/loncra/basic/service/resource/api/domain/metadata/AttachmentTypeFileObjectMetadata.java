package io.github.loncra.basic.service.resource.api.domain.metadata;

import io.github.loncra.basic.service.resource.api.enumerate.AttachmentTypeEnum;
import io.github.loncra.framework.commons.minio.FileObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 带有附件类型的文件对象 dto
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AttachmentTypeFileObjectMetadata extends FileObject {

    @Serial
    private static final long serialVersionUID = 3968451377668853504L;

    private AttachmentTypeEnum attachmentType;
}
