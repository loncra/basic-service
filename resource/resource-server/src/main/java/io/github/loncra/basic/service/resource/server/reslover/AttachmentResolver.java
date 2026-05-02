package io.github.loncra.basic.service.resource.server.reslover;

import io.github.loncra.basic.service.resource.api.enumerate.AttachmentTypeEnum;
import io.github.loncra.basic.service.resource.server.domain.body.PresignedUrlRequestBody;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.minio.FileObject;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.minio.UserMetadataFileObject;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.messages.Part;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 附件解析器
 *
 * @author maurice.chen
 */
public interface AttachmentResolver extends Ordered {

    /**
     * 是否支持附件类型
     *
     * @param attachmentType 附件类型
     *
     * @return true 是，否则 false
     */
    boolean isSupport(AttachmentTypeEnum attachmentType);

    /**
     * 删除文件前触发此方法
     *
     * @param fileObject  minio 文件对象
     * @param appendParam 附加的额外参数
     *
     * @return rest 结果集，当结果集的状态非 200 时，会终止删除
     */
    default RestResult<Map<String, Object>> preDelete(
            FileObject fileObject,
            Map<String, Object> appendParam
    ) {
        return null;
    }

    /**
     * 删除文件后触发此方法
     *
     * @param fileObject  minio 文件对象
     * @param appendParam 附加的额外参数
     */
    default void postDelete(
            FileObject fileObject,
            Map<String, Object> appendParam
    ) {

    }

    /**
     * 上传文件前触发此方法
     *
     * @param file        上传的文件
     * @param fileObject  minio 文件对象
     * @param appendParam 附加参数
     * @param token       认证信息
     *
     * @return rest 结果集，当结果集的状态非 200 时，会终止上传
     */
    default RestResult<Map<String, Object>> preUpload(
            MultipartFile file,
            UserMetadataFileObject fileObject,
            AuditAuthenticationToken token,
            Map<String, Object> appendParam
    ) {
        return null;
    }

    /**
     * 上传完成后触发此方法
     *
     * @param filenameObject 文件信息
     * @param result         响应结果
     * @param token          当前用户 token
     * @param appendParam    附加参数信息
     */
    default void postUpload(
            UserMetadataFileObject filenameObject,
            ObjectWriteResult result,
            AuditAuthenticationToken token,
            Map<String, Object> appendParam
    ) {

    }

    default RestResult<Map<String, Object>> preGetObject(
            FileObject fileObject,
            AuditAuthenticationToken token,
            Map<String, Object> appendParam
    ) {
        return null;
    }

    /**
     * 获取对象后时触发此方法
     *
     * @param fileObject  文件对象
     * @param response    spring mvc 响应对象
     * @param token       认证信息
     * @param appendParam 附加数据
     */
    default void postGetObject(
            FileObject fileObject,
            ResponseEntity<byte[]> response,
            AuditAuthenticationToken token,
            Map<String, Object> result,
            Map<String, Object> appendParam
    ) {

    }

    /**
     * 获取键名称
     *
     * @return 键名称
     */
    default String getKeyName() {
        return this.getClass()
                .getSimpleName();
    }

    /**
     * 创建分片上传时触发此方法
     *
     * @param fileObject  文件对象
     * @param appendParam 附加参数
     *
     * @return rest 结果集，当结果集的状态非 200 时，会终止上传
     */
    default RestResult<Map<String, Object>> preCreateMultipartUpload(
            FileObject fileObject,
            int size,
            Map<String, Object> appendParam
    ) {
        return null;
    }

    /**
     * 合并分排尿完成后触发此方法
     *
     * @param fileObject  文件对象
     * @param parts       合并内容
     * @param uploadId    上传 id
     * @param appendParam 附加参数
     */
    default RestResult<Map<String, Object>> preCompleteMultipartUpload(
            FileObject fileObject,
            List<Part> parts,
            String uploadId,
            Map<String, Object> appendParam
    ) {
        return null;
    }

    @Override
    default int getOrder() {
        return Integer.MIN_VALUE;
    }

    default void postCompleteMultipartUpload(
            FileObject fileObject,
            String uploadId,
            List<Part> parts,
            ObjectWriteResult result
    ) {

    }

    default RestResult<Map<String, Object>> preGetPresignedObjectUrl(
            FileObject fileObject,
            PresignedUrlRequestBody body,
            Map<String, Object> appendParam
    ) {
        return null;
    }

    default void postGetPresignedObjectUrl(
            GetPresignedObjectUrlArgs args,
            Map<String, Object> result,
            Map<String, Object> appendParam
    ) {

    }
}
