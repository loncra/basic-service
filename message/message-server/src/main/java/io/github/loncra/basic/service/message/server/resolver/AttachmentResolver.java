package io.github.loncra.basic.service.message.server.resolver;

import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.minio.FileObject;

/**
 * 附件解析器
 *
 * @author maurice.chen
 */
public interface AttachmentResolver {

    /**
     * 获取消息类型
     *
     * @return 消息类型
     */
    String getMessageType();

    /**
     * 删除附件
     *
     * @param fileObject 文件对象
     * @return rest 结果集
     */
    RestResult<Object> removeAttachment(FileObject fileObject);
}
