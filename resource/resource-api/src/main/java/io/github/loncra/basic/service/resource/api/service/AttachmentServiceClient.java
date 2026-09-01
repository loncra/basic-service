package io.github.loncra.basic.service.resource.api.service;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import io.github.loncra.basic.service.commons.config.AttachmentConfig;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.ExportDataMetadata;
import io.github.loncra.basic.service.resource.api.domain.CompleteUploadRequestBody;
import io.github.loncra.basic.service.resource.api.domain.MultipartUploadFile;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.MetadataUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.domain.metadata.TreeDescriptionMetadata;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.commons.id.metadata.IdValueRecordMetadata;
import io.github.loncra.framework.commons.minio.CopyFileObject;
import io.github.loncra.framework.commons.minio.FileObject;
import io.github.loncra.framework.commons.minio.MoveFileObject;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.commons.tree.Tree;
import io.github.loncra.framework.minio.MinioAsyncTemplate;
import io.github.loncra.framework.spring.security.core.authentication.service.feign.FeignAuthenticationConfiguration;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.http.MediaType;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * 附件服务客户端
 *
 * @author maurice.chen
 */
public interface AttachmentServiceClient {

    /**
     * 上传单个附件
     *
     * @param file         文件内容
     * @param type         桶名称
     * @param requestParam 附加参数
     *
     * @return rest 结果集
     */
    ObjectWriteResult singleUploadAttachmentFile(
            MultipartFile file,
            String type,
            Map<String, String> requestParam
    );

    /**
     * 创建分片上传
     *
     * @param type         桶类型
     * @param objectName   对象名称
     * @param contentType  内容类型
     * @param size         文件大小
     * @param appendParam  附加参数
     *
     * @return 分片初始化信息
     */
    RestResult<Map<String, Object>> createMultipartUpload(
            String type,
            String objectName,
            String contentType,
            Integer size,
            Map<String, Object> appendParam
    );

    /**
     * 上传分片
     *
     * @param file       分片内容
     * @param partNumber 分片序号（从 1 开始）
     * @param uploadId   分片上传 id
     *
     * @return 分片结果
     */
    RestResult<Map<String, Object>> uploadPart(
            MultipartFile file,
            int partNumber,
            String uploadId
    );

    /**
     * 完成分片上传
     *
     * @param body        完成分片上传请求体
     * @param appendParam 附加参数
     *
     * @return 写入结果
     */
    ObjectWriteResult completeMultipartUpload(
            @RequestBody
            CompleteUploadRequestBody body,
            Map<String, Object> appendParam
    );

    /**
     * 按文件大小自动选择单次或分片上传。
     *
     * @param file         文件内容
     * @param type         桶类型
     * @param requestParam 附加参数
     *
     * @return 写入结果
     */
    default ObjectWriteResult uploadAttachmentFile(
            MultipartFile file,
            String type,
            Map<String, String> requestParam
    ) {
        SystemException.isTrue(Objects.nonNull(file) && file.getSize() > 0, "上传文件不能为空");
        if (file.getSize() < AttachmentConfig.DEFAULT_UPLOAD_BLOCK_SIZE) {
            return singleUploadAttachmentFile(file, type, requestParam);
        }
        return multipartUploadAttachmentFile(file, type, requestParam);
    }

    private ObjectWriteResult multipartUploadAttachmentFile(
            MultipartFile file,
            String type,
            Map<String, String> requestParam
    ) {
        String objectName = StringUtils.defaultIfBlank(file.getOriginalFilename(), file.getName());
        String contentType = StringUtils.defaultIfBlank(file.getContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE);
        Map<String, Object> appendParam = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(requestParam)) {
            appendParam.putAll(requestParam);
        }

        RestResult<Map<String, Object>> initResult = createMultipartUpload(
                type,
                objectName,
                contentType,
                Math.toIntExact(file.getSize()),
                appendParam
        );
        SystemException.isTrue(
                Objects.nonNull(initResult) && initResult.isSuccess() && MapUtils.isNotEmpty(initResult.getData()),
                "创建分片上传失败"
        );

        Map<String, Object> initData = initResult.getData();
        int chunk = toInt(initData.get("chunk"));
        int uploadBlockSize = toInt(initData.get("uploadBlockSize"));
        String uploadId = Objects.toString(initData.get("uploadId"), StringUtils.EMPTY);
        SystemException.isTrue(chunk > 0 && StringUtils.isNotEmpty(uploadId), "没有可使用的分片上传路径");

        byte[] content = SystemException.convertSupplier(file::getBytes, "读取上传文件失败");
        List<IdValueMetadata<String, Integer>> parts = new LinkedList<>();
        for (int i = 1; i <= chunk; i++) {
            int from = (i - 1) * uploadBlockSize;
            int to = (int) Math.min((long) i * uploadBlockSize, content.length);
            MultipartUploadFile partFile = new MultipartUploadFile(
                    MultipartUploadFile.DEFAULT_FILE_NAME,
                    objectName,
                    contentType,
                    Arrays.copyOfRange(content, from, to)
            );
            RestResult<Map<String, Object>> partResult = uploadPart(partFile, i, uploadId);
            SystemException.isTrue(
                    Objects.nonNull(partResult) && partResult.isSuccess() && MapUtils.isNotEmpty(partResult.getData()),
                    "分片上传响应无效"
            );
            Map<String, Object> partData = partResult.getData();
            parts.add(IdValueMetadata.of(
                    Objects.toString(partData.get("etag"), StringUtils.EMPTY),
                    toInt(partData.get("partNumber"))
            ));
        }

        CompleteUploadRequestBody body = new CompleteUploadRequestBody();
        body.setUploadId(uploadId);
        body.setParts(parts);
        return completeMultipartUpload(body, appendParam);
    }

    private static int toInt(Object value) {
        SystemException.isTrue(value instanceof Number, "分片上传响应字段无效");
        return ((Number) value).intValue();
    }

    /**
     * 获取文件
     *
     * @param type 桶类型
     * @param objectName 对象名称
     *
     * @return 字节流
     */
    byte[] getAttachmentFile(
            String type,
            String objectName
    );

    /**
     * 按类型与对象前缀列举附件
     *
     * @param type 桶类型
     * @param filename 对象前缀 / 文件名
     * @param formatObjectWriteResult true 时返回可反序列化的 {@link ObjectWriteResult}；跨服务调用必须为 true
     *
     * @return 对象列表
     */
    List<ObjectWriteResult> findAttachment(
            String type,
            String filename,
            boolean recursive,
            boolean formatObjectWriteResult
    );

    /**
     * 删除附件
     *
     * @param fileObjects 文件对象集合
     * @param appendParam 附加参数内容
     *
     * @return rest 结果集
     */
    RestResult<Object> deleteAttachment(
            List<FileObject> fileObjects,
            Map<String, Object> appendParam
    );


    /**
     * 移动附件
     *
     * @param moveFileObject 移动文件对象
     *
     * @return rest 结果集
     */
    ObjectWriteResult moveAttachment(
            @RequestBody
            MoveFileObject moveFileObject
    );

    /**
     * 拷贝附件
     *
     * @param fileObject 移动文件对象
     *
     * @return rest 结果集
     */
    ObjectWriteResult copyAttachment(
            @RequestBody
            CopyFileObject fileObject
    );

    /**
     * 导出数据内容
     *
     * @param metadata 数据结果表叔
     * @param data     数据内容
     * @param dto      导出元数据信息
     * @param function 映射数据内容
     *
     */
    default void export(
            TreeDescriptionMetadata metadata,
            List<Object> data,
            ExportDataMetadata dto,
            Function<IdValueRecordMetadata<String, Object>, Object> function
    )  {

        List<Map<String, Object>> convert = new LinkedList<>();

        for (Object o : data) {

            Map<String, Object> map = new LinkedHashMap<>();

            for (Tree<String, TreeDescriptionMetadata> child : metadata.getChildren()) {
                TreeDescriptionMetadata description = CastUtils.cast(child, TreeDescriptionMetadata.class);
                Object value;
                if (Field.class.getName().equals(description.getSource())) {
                    Field field = Objects.requireNonNull(ReflectionUtils.findField(o.getClass(), description.getId()));
                    field.setAccessible(true);
                    value = ReflectionUtils.getField(field, o);
                }
                else if (Method.class.getName().equals(description.getSource())) {
                    String methodName = Strings.CS.removeEnd(Strings.CS.removeEnd(description.getId(), MetadataUtils.RIGHT_BRACKET), MetadataUtils.LEFT_BRACKET);
                    Method method = Objects.requireNonNull(ReflectionUtils.findMethod(o.getClass(), methodName));
                    value = ReflectionUtils.invokeMethod(method, o);
                }
                else if (Class.class.getName().equals(description.getSource())) {
                    Field field = Objects.requireNonNull(ReflectionUtils.findField(o.getClass(), description.getId()));
                    field.setAccessible(true);
                    value = Objects.requireNonNull(ReflectionUtils.getField(field, o), String::new);
                }
                else {
                    throw new SystemException("找不到来源为:" + description.getSource() + ", 的解析内容支持。");
                }

                IdValueRecordMetadata<String, Object> recordMetadata = new IdValueRecordMetadata<>();

                if (value instanceof NameEnum nameEnum) {
                    value = nameEnum.getName();
                }
                else if (value instanceof ValueEnum<?> valueEnum) {
                    value = valueEnum.getValue();
                }

                recordMetadata.setId(description.getId());
                recordMetadata.setValue(value);
                recordMetadata.setRecord(o);

                Object text = function.apply(recordMetadata);
                map.put(description.getName(), text);
            }

            if (MapUtils.isNotEmpty(map)) {
                convert.add(map);
            }

        }

        SystemException.isTrue(CollectionUtils.isNotEmpty(convert), "当前需要导出的数据内容为空，但实际导出内容存在 (" + data.size() + ") 条数据");

        ExcelWriter writer = ExcelUtil.getWriter(true);
        //合并单元格后的标题行，使用默认标题样式
        writer.merge(metadata.getChildren()
                             .size() - 1, dto.getFilename());
        writer.write(convert, true);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writer.flush(outputStream, true);
        writer.close();

        dto.setSize(outputStream.toByteArray().length);

        Map<String, String> appendParams = new LinkedHashMap<>();
        if (StringUtils.isNotEmpty(dto.getPrincipal())) {
            appendParams.put(MinioAsyncTemplate.AMZ_META_UPLOADER_ID, dto.getPrincipal());
        }

        ObjectWriteResult writeResult = uploadAttachmentFile(
                new MultipartUploadFile(dto.toUploadFilename(), dto.getFilename(), MediaType.APPLICATION_OCTET_STREAM_VALUE, outputStream.toByteArray()),
                SystemConstants.EXPORT_BUCKET.getBucketName(),
                appendParams
        );

        if (Objects.nonNull(writeResult)) {
            dto.getMetadata()
                    .put(RestResult.DEFAULT_DATA_NAME, writeResult);
            ExecuteStatus.success(dto);
        }
        else {
            ExecuteStatus.failure(dto, "上传文件错误");
        }
    }


    static boolean isInaccessible(
            AuditAuthenticationToken token,
            Map<String, String> userMetadata
    ) {
        if (FeignAuthenticationConfiguration.DEFAULT_TYPE.equals(token.getType())) {
            return true;
        }
        if (MapUtils.isEmpty(userMetadata)) {
            return true;
        }
        Map<String, String> metadata = new LinkedHashMap<>();
        userMetadata.forEach((k,v) -> metadata.put(k.toLowerCase(), v));
        String uploaderId = Objects.toString(metadata.get(MinioAsyncTemplate.AMZ_META_UPLOADER_ID), StringUtils.EMPTY);

        if (StringUtils.isEmpty(uploaderId)) {
            uploaderId = Objects.toString(metadata.get(MinioAsyncTemplate.UPLOADER_ID), StringUtils.EMPTY);
        }

        return token.getName()
                .equals(uploaderId);
    }
}
