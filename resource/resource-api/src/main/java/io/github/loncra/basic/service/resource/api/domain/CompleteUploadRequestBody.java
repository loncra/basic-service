package io.github.loncra.basic.service.resource.api.domain;

import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.commons.minio.FileObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * 分片上传完成请求体
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompleteUploadRequestBody extends FileObject {

    @Serial
    private static final long serialVersionUID = -5171636355118685747L;

    /**
     * 分片上传 id
     */
    private String uploadId;

    /**
     * 分片数据集合
     */
    private List<IdValueMetadata<String, Integer>> parts;
}
