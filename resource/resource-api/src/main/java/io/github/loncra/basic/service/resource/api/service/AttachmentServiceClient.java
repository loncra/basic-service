package io.github.loncra.basic.service.resource.api.service;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.ExportDataMetadata;
import io.github.loncra.basic.service.resource.api.domain.MultipartUploadFile;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.MetadataUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.domain.metadata.TreeDescriptionMetadata;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdValueRecordMetadata;
import io.github.loncra.framework.commons.minio.CopyFileObject;
import io.github.loncra.framework.commons.minio.FileObject;
import io.github.loncra.framework.commons.minio.MoveFileObject;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.commons.tree.Tree;
import io.github.loncra.framework.minio.MinioAsyncTemplate;
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
     * 获取文件
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     *
     * @return 字节流
     */
    byte[] getAttachmentFile(
            String bucketName,
            String objectName
    );

    /**
     * 判断文件是否存在
     *
     * @param bucketName 同名称
     * @param objectName 对象名称
     *
     * @return true 存在，否则 false
     */
    /*boolean isAttachmentFileExist(
            String bucketName,
            String objectName
    );*/

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

        ObjectWriteResult writeResult = singleUploadAttachmentFile(
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
}
