package io.github.loncra.basic.service.commons.domain;

import io.github.loncra.framework.commons.minio.ObjectWriteResult;

import java.util.List;

/**
 * 带附件的消息
 *
 * @author maurice.chen
 */
public interface AttachmentMessage {

    String ATTACHMENT_LIST_FIELD = "attachmentList";

    /**
     * 获取附件信息集合
     *
     * @return 附件信息集合
     */
    List<ObjectWriteResult> getAttachmentList();
}
