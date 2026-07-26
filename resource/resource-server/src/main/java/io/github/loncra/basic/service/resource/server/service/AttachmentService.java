package io.github.loncra.basic.service.resource.server.service;

import com.alibaba.nacos.common.utils.CollectionUtils;
import io.github.loncra.basic.service.commons.config.AttachmentConfig;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.ExportDataMetadata;
import io.github.loncra.basic.service.resource.api.domain.metadata.AttachmentTypeFileObjectMetadata;
import io.github.loncra.basic.service.resource.api.enumerate.AttachmentTypeEnum;
import io.github.loncra.basic.service.resource.api.service.AttachmentServiceClient;
import io.github.loncra.basic.service.resource.server.domain.body.CompleteUploadRequestBody;
import io.github.loncra.basic.service.resource.server.domain.body.PresignedUrlRequestBody;
import io.github.loncra.basic.service.resource.server.reslover.AttachmentResolver;
import io.github.loncra.basic.service.resource.server.service.dictionary.DataDictionaryService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.commons.exception.ErrorCodeException;
import io.github.loncra.framework.commons.exception.ServiceException;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdNameValueMetadata;
import io.github.loncra.framework.commons.jackson.serializer.DesensitizeSerializer;
import io.github.loncra.framework.commons.minio.*;
import io.github.loncra.framework.commons.tenant.TenantContext;
import io.github.loncra.framework.commons.tenant.holder.TenantContextHolder;
import io.github.loncra.framework.crypto.algorithm.CodecUtils;
import io.github.loncra.framework.minio.MinioAsyncTemplate;
import io.github.loncra.framework.minio.ObjectItem;
import io.github.loncra.framework.minio.UserMetadataFileObject;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.web.mvc.SpringMvcUtils;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.InitiateMultipartUploadResult;
import io.minio.messages.Item;
import io.minio.messages.Part;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.KeysScanOptions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 附件工具服务
 *
 * @author maurice.chen
 */
@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class AttachmentService implements InitializingBean {

    private final List<AttachmentResolver> attachmentResolvers;

    private final AttachmentConfig attachmentConfig;

    private final MinioAsyncTemplate minioAsyncTemplate;

    @Getter
    private final RedissonClient redissonClient;

    private final DataDictionaryService dataDictionaryService;

    /**
     * 转换目标对象和目标类的字段为 map
     *
     * @param target       目标对象
     * @param targetClass  目标类
     * @param ignoreFields 要忽略的字段名
     *
     * @return map 对象
     */
    public Map<String, Object> convertFields(
            Object target,
            Class<?> targetClass,
            List<String> ignoreFields
    ) {

        Map<String, Object> result = new LinkedHashMap<>();

        List<Field> fieldList = Arrays.asList(targetClass.getDeclaredFields());

        fieldList.stream()
                .filter(field -> !ignoreFields.contains(field.getName()))
                .forEach(field -> result.put(field.getName(), getFieldToValue(target, field)));

        if (Objects.nonNull(targetClass.getSuperclass())) {
            result.putAll(convertFields(target, targetClass.getSuperclass(), ignoreFields));
        }

        return result;
    }

    /**
     * 获取字段的 toString 值
     *
     * @param target 目标对象
     * @param field  字段
     *
     * @return 值
     */
    private Object getFieldToValue(
            Object target,
            Field field
    ) {

        field.setAccessible(true);
        Object value = ReflectionUtils.getField(field, target);

        if (Objects.isNull(value)) {
            return null;
        }

        if (Strings.CS.startsWith(value.toString(), ObjectWriteResult.MINIO_ETAG_QUOTATION_MARKS) && (Strings.CS.endsWith(value.toString(), ObjectWriteResult.MINIO_ETAG_QUOTATION_MARKS))) {
            value = StringUtils.unwrap(value.toString(), ObjectWriteResult.MINIO_ETAG_QUOTATION_MARKS);
        }

        if (ZonedDateTime.class.isAssignableFrom(value.getClass())) {
            ZonedDateTime zonedDateTime = CastUtils.cast(value);
            return Date.from(zonedDateTime.toInstant());
        }

        return value;
    }

    public FileObject getFileObject(
            String type,
            String objectName
    ) {

        AttachmentTypeEnum attachmentType = ValueEnum.ofEnum(AttachmentTypeEnum.class, type, true);
        if (Objects.nonNull(attachmentType)) {
            AttachmentTypeFileObjectMetadata metadata = new AttachmentTypeFileObjectMetadata();
            metadata.setAttachmentType(attachmentType);
            metadata.setObjectName(objectName);
            metadata.setBucketName(attachmentConfig.getBucketName(attachmentType.getValue()));
            return metadata;
        }
        else {
            return FileObject.of(type, objectName);
        }
    }

    public Map<String, RestResult<Map<String, Object>>> executeRestResultAttachmentResolver(
            AttachmentTypeEnum attachmentType,
            Function<AttachmentResolver, RestResult<Map<String, Object>>> fn
    ) {
        Map<String, RestResult<Map<String, Object>>> result = new LinkedHashMap<>();
        List<AttachmentResolver> attachmentResolvers = this.attachmentResolvers.stream()
                .filter(a -> a.isSupport(attachmentType))
                .toList();
        for (AttachmentResolver resolver : attachmentResolvers) {
            RestResult<Map<String, Object>> execute = fn.apply(resolver);

            if (Objects.isNull(execute)) {
                continue;
            }

            if (!execute.isSuccess()) {
                return Map.of(resolver.getKeyName(), execute);
            }

            if (MapUtils.isNotEmpty(execute.getData())) {
                result.put(resolver.getKeyName(), execute);
            }
        }

        return result;
    }

    public boolean executeRestResultAttachmentResolver(
            FileObject fileObject,
            Map<String, Object> result,
            Function<AttachmentResolver, RestResult<Map<String, Object>>> fn
    ) {

        if (!AttachmentTypeFileObjectMetadata.class.isAssignableFrom(fileObject.getClass())) {
            return true;
        }

        AttachmentTypeFileObjectMetadata metadata = CastUtils.cast(fileObject);
        Map<String, RestResult<Map<String, Object>>> resolverResult = executeRestResultAttachmentResolver(metadata.getAttachmentType(), fn);
        Optional<Map.Entry<String, RestResult<Map<String, Object>>>> optional = resolverResult
                .entrySet()
                .stream()
                .filter(e -> !e.getValue().isSuccess())
                .findFirst();
        if (optional.isPresent()) {
            result.put(optional.get().getKey(), optional.get().getValue());
            return false;
        }
        resolverResult.forEach((key, value) -> result.put(key, value.getData()));

        return true;
    }

    public void executeVoidAttachmentResolver(
            FileObject fileObject,
            Consumer<AttachmentResolver> consumer
    ) {

        if (!AttachmentTypeFileObjectMetadata.class.isAssignableFrom(fileObject.getClass())) {
            return;
        }

        AttachmentTypeFileObjectMetadata dto = CastUtils.cast(fileObject);
        this.attachmentResolvers.stream()
                .filter(a -> a.isSupport(dto.getAttachmentType()))
                .forEach(consumer);
    }

    public Map<String, Object> uploadPart(
            MultipartFile file,
            int partNumber,
            String uploadId
    ) throws Exception {
        FilenameObject filenameObject = getMultipartUploadCache(uploadId);

        Part response = minioAsyncTemplate
                .uploadPartAsync(
                        filenameObject,
                        uploadId,
                        file.getInputStream(),
                        file.getSize(),
                        partNumber
                )
                .get();

        return convertFields(response, Part.class, new LinkedList<>());
    }

    /**
     * 创建上传分片
     *
     * @param fileObject  文件对象
     * @param size        文件大小
     * @param appendParam 附加参数
     *
     * @return 分配上传内容
     */
    public Map<String, Object> createMultipartUpload(
            FileObject fileObject,
            String contentType,
            int size,
            Map<String, Object> appendParam
    ) throws Exception {
        String msg = "文件大小 (" + size + ") 小于等于分片最小大小 (" + attachmentConfig.getUploadBlockSize() + ") 不需要分片上传";
        SystemException.isTrue(
                size > attachmentConfig.getUploadBlockSize(),
                () -> new ErrorCodeException(msg, ErrorCodeException.NO_CONTENT_CODE)
        );

        Map<String, Object> result = new LinkedHashMap<>();

        boolean execute = executeRestResultAttachmentResolver(
                fileObject,
                result,
                resolver -> resolver.preCreateMultipartUpload(fileObject, size, appendParam)
        );

        if (!execute) {
            return result;
        }

        FilenameObject filenameObject = FilenameObject.of(fileObject);
        filenameObject.getExtraHeaders()
                .put(HttpHeaders.CONTENT_TYPE, contentType);

        result.putAll(CastUtils.convertValue(filenameObject, CastUtils.MAP_TYPE_REFERENCE));
        result.put(SystemConstants.APPEND_PARAM_FIELD_NAME, appendParam);
        result.put(attachmentConfig.getUploadBlockSizeParamName(), attachmentConfig.getUploadBlockSize());

        double chunkSize = Math.ceil((double) size / (double) attachmentConfig.getUploadBlockSize());
        InitiateMultipartUploadResult uploadResult = minioAsyncTemplate
                .createMultipartUploadAsync(filenameObject)
                .get()
                .result();

        result.put(attachmentConfig.getUploadIdParamName(), uploadResult.uploadId());
        result.put(attachmentConfig.getChunkParamName(), chunkSize);

        String key = attachmentConfig.getMultipartUploadCache()
                .getName(uploadResult.uploadId());
        RBucket<Map<String, Object>> bucket = redissonClient.getBucket(key);

        TimeProperties expiresTime = attachmentConfig.getMultipartUploadCache()
                .getExpiresTime();
        bucket.setAsync(result, expiresTime.getValue(), expiresTime.getUnit());

        return result;

    }

    private FilenameObject getMultipartUploadCache(String uploadId) {
        String key = attachmentConfig.getMultipartUploadCache()
                .getName(uploadId);
        RBucket<Map<String, Object>> bucket = redissonClient.getBucket(key);
        SystemException.isTrue(bucket.isExists(), "找不到 ID 为 [" + uploadId + "] 分片上传内容");
        return CastUtils.convertValue(bucket.get(), FilenameObject.class);
    }

    public ObjectWriteResult completeMultipartUpload(
            CompleteUploadRequestBody body,
            Map<String, Object> appendParam
    ) throws Exception {

        FilenameObject filenameObject = getMultipartUploadCache(body.getUploadId());

        List<Part> parts = body
                .getParts()
                .stream()
                .map(metadata -> new Part(metadata.getValue(), metadata.getId()))
                .toList();
        Map<String, Object> result = new LinkedHashMap<>();

        boolean execute = executeRestResultAttachmentResolver(
                filenameObject,
                result,
                resolver -> resolver.preCompleteMultipartUpload(filenameObject, parts, body.getUploadId(), appendParam)
        );
        if (!execute) {
            return null;
        }

        minioAsyncTemplate.completeMultipartUploadAsync(filenameObject, parts.toArray(Part[]::new), body.getUploadId())
                .get();

        ObjectWriteResult objectWriteResult = createObjectWriteResponseResult(filenameObject);

        executeVoidAttachmentResolver(filenameObject, resolver -> resolver.postCompleteMultipartUpload(filenameObject, body.getUploadId(), parts, objectWriteResult));

        return objectWriteResult;
    }

    private ObjectWriteResult createObjectWriteResponseResult(
            FileObject fileObject
    ) throws Exception {
        ObjectWriteResult result = CastUtils.of(fileObject, ObjectWriteResult.class);
        if (fileObject instanceof UserMetadataFileObject userMetadataFileObject && MapUtils.isNotEmpty(userMetadataFileObject.getUserMetadata())) {
            result.getExtraHeaders()
                    .putAll(userMetadataFileObject.getUserMetadata());
        }
        StatObjectResponse stat = minioAsyncTemplate.statObject(
                StatObjectArgs.builder()
                        .bucket(result.getBucketName())
                        .object(result.getObjectName())
                        .build()
        ).get();
        result.setSize(stat.size());
        result.setEtag(stat.etag());
        return result;
    }

    public ObjectWriteResult moveObject(MoveFileObject object) throws Exception {
        minioAsyncTemplate.moveObject(object)
                .get();
        return createObjectWriteResponseResult(object.getTarget());
    }

    public ObjectWriteResult copyObject(CopyFileObject object) throws Exception {
        minioAsyncTemplate.copyObject(object.getSource(), object.getTarget())
                .get();
        return createObjectWriteResponseResult(object.getTarget());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (AttachmentTypeEnum type : AttachmentTypeEnum.values()) {
            minioAsyncTemplate.makeBucketIfNotExists(Bucket.of(attachmentConfig.getBucketName(type.getValue())));
        }

        minioAsyncTemplate.makeBucketIfNotExists(SystemConstants.EXPORT_BUCKET);
    }

    public List<Map<String, Object>> buckets() throws Exception {
        Map<String, Object> data = minioAsyncTemplate.buckets(StringUtils.EMPTY)
                .get();

        List<Map<String, Object>> buckets = CastUtils.cast(data.get(MinioAsyncTemplate.BUCKETS_API_NAME));
        List<String> attachmentBuckets = Arrays.stream(AttachmentTypeEnum.values())
                .map(a -> attachmentConfig.getBucketName(a.getValue()))
                .toList();
        buckets.removeIf(bucket -> !attachmentBuckets.contains(bucket.get(NameEnum.FIELD_NAME).toString()));

        for (AttachmentTypeEnum a : AttachmentTypeEnum.values()) {
            buckets.stream()
                    .filter(bucket -> attachmentConfig.getBucketName(a.getValue()).equals(bucket.get(NameEnum.FIELD_NAME).toString()))
                    .findFirst()
                    .ifPresent(bucket -> setBucketValue(bucket, a));
        }

        return buckets;
    }

    private void setBucketValue(
            @NotNull Map<String, Object> bucket,
            AttachmentTypeEnum a
    ) {
        bucket.put(NameEnum.FIELD_NAME, a.getName());
        bucket.put(IdNameValueMetadata.VALUE_FIELD_NAME, attachmentConfig.getBucketName(a.getValue()));
    }

    public String getObjectResponseFilename(
            GetObjectResponse is,
            FileObject fileObject,
            String defaultName
    ) {
        String filename = StringUtils.EMPTY;

        if (StringUtils.isEmpty(defaultName) && fileObject instanceof FilenameObject) {
            FilenameObject filenameObject = CastUtils.cast(fileObject, FilenameObject.class);
            filename = filenameObject.getFilename();
        }

        if (StringUtils.isEmpty(filename)) {
            filename = is.headers()
                    .get(FilenameObject.MINIO_ORIGINAL_FILE_NAME);
        }

        if (StringUtils.isEmpty(filename)) {
            filename = defaultName;
        }

        if (StringUtils.isEmpty(filename)) {
            filename = fileObject.getObjectName();
        }

        if (Strings.CS.contains(filename, AntPathMatcher.DEFAULT_PATH_SEPARATOR)) {
            filename = StringUtils.substringAfterLast(filename, AntPathMatcher.DEFAULT_PATH_SEPARATOR);
        }

        return System.currentTimeMillis() + CastUtils.UNDERSCORE + filename;
    }

    public List<ObjectItem> list(
            FileObject fileObject,
            AuditAuthenticationToken token
    ) throws Exception {

        ListObjectsArgs args = ListObjectsArgs.builder()
                .bucket(fileObject.getBucketName())
                .fetchOwner(true)
                .prefix(fileObject.getObjectName())
                .includeUserMetadata(true)
                .recursive(true)
                .build();

        Iterable<Result<Item>> iterable = minioAsyncTemplate.listObjects(args);

        Stream<ObjectItem> streamed = minioAsyncTemplate.covertObjectItem(iterable)
                .stream();

        if (Objects.nonNull(token)) {
            streamed = streamed.filter(o -> AttachmentServiceClient.isInaccessible(token, o.getUserMetadata()));
        }

        return streamed.toList();
    }

    public List<Map<String, Object>> delete(
            List<FileObject> fileObjects,
            AuditAuthenticationToken token,
            Map<String, Object> appendParam
    ) throws Exception {
        List<Map<String, Object>> result = new LinkedList<>();

        for (FileObject object : fileObjects) {

            if (Strings.CS.endsWith(object.getObjectName(), AntPathMatcher.DEFAULT_PATH_SEPARATOR)) {

                FileObject listFileObject = FileObject.of(Strings.CS.removeStart(object.getBucketName(), attachmentConfig.getBucketPrefix()), object.getObjectName());

                List<ObjectItem> listFile = list(listFileObject, token);

                List<FileObject> fileObjectList = listFile.stream()
                        .map(f -> FileObject.of(listFileObject.getBucketName(), f.getObjectName()))
                        .peek(f -> f.setBucketName(Strings.CS.removeStart(f.getBucketName(), attachmentConfig.getBucketPrefix())))
                        .collect(Collectors.toList());

                result.addAll(delete(fileObjectList, token, appendParam));
            }
            else {
                StatObjectArgs args = StatObjectArgs.builder()
                        .bucket(object.getBucketName())
                        .object(object.getObjectName())
                        .build();
                StatObjectResponse statObjectResponse = minioAsyncTemplate
                        .statObject(args)
                        .get();

                if (Objects.nonNull(token) && !AttachmentServiceClient.isInaccessible(token, statObjectResponse.userMetadata())) {
                    continue;
                }

                Map<String, Object> item = new LinkedHashMap<>();
                boolean execute = executeRestResultAttachmentResolver(object, item, r -> r.preDelete(object, appendParam));
                if (!execute) {
                    result.add(item);
                    continue;
                }

                minioAsyncTemplate.deleteObject(object, false);

                executeVoidAttachmentResolver(object, r -> r.postDelete(object, appendParam));
                if (MapUtils.isNotEmpty(item)) {
                    result.add(item);
                }
            }
        }

        return result;
    }

    public ResponseEntity<byte[]> getObjectResponseEntity(
            FileObject fileObject,
            Map<String, Object> appendParam
    ) throws Exception {

        GetObjectResponse is = minioAsyncTemplate.getObject(fileObject)
                .get();

        HttpHeaders headers = new HttpHeaders();

        String contentType = is.headers()
                .get(HttpHeaders.CONTENT_TYPE);
        if (StringUtils.isNotEmpty(contentType)) {
            MediaType mediaType = MediaType.parseMediaType(contentType);
            headers.setContentType(mediaType);
        }

        byte[] data = IOUtils.toByteArray(is);
        is.close();

        if (BooleanUtils.toBoolean(Objects.toString(appendParam.get(SystemConstants.DOWNLOAD_FIELD_NAME), Boolean.FALSE.toString()))) {
            String filename = getObjectResponseFilename(
                    is,
                    fileObject,
                    appendParam.getOrDefault(FilenameObject.MINIO_ORIGINAL_FILE_NAME, StringUtils.EMPTY).toString()
            );

            headers.setContentDispositionFormData(
                    SpringMvcUtils.DEFAULT_ATTACHMENT_NAME,
                    URLEncoder.encode(filename, CodecUtils.DEFAULT_ENCODING)
            );

            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        }
    }

    public Map<String, Object> getPresignedObjectUrl(
            FileObject fileObject,
            PresignedUrlRequestBody body,
            Map<String, Object> appendParam
    ) throws Exception {


        TimeProperties expiry = body.getTimeProperties();
        if (Objects.isNull(expiry)) {
            expiry = attachmentConfig.getPresignedTime();
        }

        Map<String, Object> result = new LinkedHashMap<>(appendParam);
        boolean execute = executeRestResultAttachmentResolver(
                fileObject,
                result,
                a -> a.preGetPresignedObjectUrl(fileObject, body, appendParam));
        if (!execute) {
            return result;
        }

        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs
                .builder()
                .bucket(fileObject.getBucketName())
                .region(fileObject.getRegion())
                .object(fileObject.getObjectName())
                .expiry((int) expiry.getValue(), expiry.getUnit())
                .method(Method.valueOf(body.getMethod()))
                .build();

        String url = minioAsyncTemplate.getPresignedObjectUrl(args);
        result.put(RestResult.DEFAULT_URL_NAME, url);

        executeVoidAttachmentResolver(fileObject, a -> a.postGetPresignedObjectUrl(args, result, appendParam));

        return result;
    }

    /**
     * 获取文件对象
     *
     * @param fileObject 文件对象信息
     *
     * @return 文件字节响应实体
     */
    public ResponseEntity<byte[]> getObject(
            FileObject fileObject,
            AuditAuthenticationToken token,
            Map<String, Object> appendParam
    ) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        boolean execute = executeRestResultAttachmentResolver(
                fileObject,
                result,
                a -> a.preGetObject(fileObject, token, appendParam)
        );
        if (!execute) {
            String message = "对 getObject 拦截器处理时出现 " + result.keySet() + "解析错误";
            List<String> list = result
                    .values()
                    .stream()
                    .filter(v -> RestResult.class.isAssignableFrom(v.getClass()))
                    .map(v -> CastUtils.cast(v, RestResult.class))
                    .map(RestResult::getMessage)
                    .toList();
            if (CollectionUtils.isNotEmpty(list)) {
                message += ",错误信息为:" + list;
            }
            throw new ServiceException(message);
        }

        ResponseEntity<byte[]> response = getObjectResponseEntity(fileObject, appendParam);
        executeVoidAttachmentResolver(fileObject, r -> r.postGetObject(fileObject, response, token, result, appendParam));

        return response;
    }

    public String getCleanUtf8(String input) {
        // 1. 去除换行符
        String cleaned = input.replaceAll("[\r\n]", "");

        // 2. 转换为 UTF-8 编码
        byte[] utf8Bytes = cleaned.getBytes(StandardCharsets.UTF_8);
        return new String(utf8Bytes, StandardCharsets.UTF_8);
    }

    public ObjectWriteResult singleUpload(
            MultipartFile file,
            String type,
            AuditAuthenticationToken token,
            Map<String, Object> appendParam
    ) throws Exception {

        FileObject fileObject = getFileObject(type, StringUtils.EMPTY);

        Map<String, String> extraHeaders = fileObject.getExtraHeaders();
        if (Objects.isNull(extraHeaders)) {
            extraHeaders = new LinkedHashMap<>();
            fileObject.setExtraHeaders(extraHeaders);
        }

        setExtraHeaders(fileObject, token);

        if (StringUtils.isNotEmpty(file.getOriginalFilename())) {
            fileObject.setObjectName(getCleanUtf8(file.getOriginalFilename()));
        }

        FilenameObject filenameObject = FilenameObject.of(fileObject);
        UserMetadataFileObject userMetadataFileObject = new UserMetadataFileObject(filenameObject);

        Map<String, Object> result = new LinkedHashMap<>();

        boolean execute = executeRestResultAttachmentResolver(
                fileObject,
                result,
                a -> a.preUpload(file, userMetadataFileObject, token, appendParam)
        );

        if (!execute) {
            return null;
        }

        ObjectWriteResponse response = minioAsyncTemplate.putObject(userMetadataFileObject, file.getInputStream(), file.getSize(), file.getContentType())
                .get();

        if (StringUtils.isNotEmpty(file.getContentType())) {
            userMetadataFileObject.getUserMetadata()
                    .put(HttpHeaders.CONTENT_TYPE, file.getContentType());
        }

        ObjectWriteResult objectWriteResult = CastUtils.of(userMetadataFileObject, ObjectWriteResult.class);
        if (MapUtils.isEmpty(objectWriteResult.getExtraHeaders())) {
            objectWriteResult.setExtraHeaders(new LinkedHashMap<>());
        }

        if (MapUtils.isNotEmpty(userMetadataFileObject.getUserMetadata())) {
            objectWriteResult.getExtraHeaders()
                    .putAll(userMetadataFileObject.getUserMetadata());
        }
        objectWriteResult.setSize(file.getSize());
        objectWriteResult.setEtag(response.etag());
        executeVoidAttachmentResolver(fileObject, r -> r.postUpload(userMetadataFileObject, objectWriteResult, token, appendParam));

        return objectWriteResult;
    }

    public void setExtraHeaders(
            FileObject fileObject,
            Authentication authentication
    ) {
        if (Objects.isNull(authentication)) {
            return;
        }
        if (!AuditAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            return;
        }

        AuditAuthenticationToken token = CastUtils.cast(authentication);
        Map<String, String> extraHeaders = fileObject.getExtraHeaders();
        if (MapUtils.isEmpty(extraHeaders)) {
            extraHeaders = new LinkedHashMap<>();
        }
        extraHeaders.put(MinioAsyncTemplate.AMZ_META_UPLOADER_ID, token.getName());
        TenantContext tenantContext = TenantContextHolder.get();
        if (Objects.nonNull(tenantContext) && Objects.nonNull(tenantContext.getId())) {
            extraHeaders.put(MinioAsyncTemplate.AMZ_META_TENANT_ID, tenantContext.getId().toString());
        }
        fileObject.setExtraHeaders(extraHeaders);
    }


    public List<ExportDataMetadata> findUserExport(AuditAuthenticationToken token) {
        String principalCache = SystemConstants.USER_EXPORT_CACHE.getName(token.getName());
        List<ExportDataMetadata> result = new LinkedList<>();
        KeysScanOptions options = KeysScanOptions.defaults()
                .pattern(principalCache + DesensitizeSerializer.DEFAULT_DESENSITIZE_SYMBOL);
        for (String key : redissonClient.getKeys().getKeys(options)) {
            RBucket<ExportDataMetadata> data = redissonClient.getBucket(key);
            if (!data.isExists()) {
                continue;
            }
            result.add(data.get());
        }
        return result.stream()
                .sorted(Comparator.comparing(ExportDataMetadata::getCreationTime).reversed())
                .toList();
    }

    public void deleteUserExport(
            AuditAuthenticationToken token,
            List<String> ids
    ) {
        List<ExportDataMetadata> data = findUserExport(token);
        List<String> keys = data.stream()
                .filter(s -> ids.contains(s.getId()))
                .peek(this::deleteUserExportAttachment)
                .map(ExportDataMetadata::toExportCacheName)
                .map(SystemConstants.USER_EXPORT_CACHE::getName)
                .toList();
        redissonClient.getKeys()
                .delete(keys.toArray(new String[0]));
    }

    private void deleteUserExportAttachment(ExportDataMetadata exportDataDto) {
        Object data = exportDataDto.getMetadata()
                .get(RestResult.DEFAULT_DATA_NAME);
        if (Objects.isNull(data)) {
            return;
        }
        ObjectWriteResult result = CastUtils.convertValue(data, ObjectWriteResult.class);
        try {
            minioAsyncTemplate.deleteObject(result, true);
        }
        catch (Exception e) {
            log.warn("删除用户导出文件的 minio 数据出现异常", e);
        }
    }
}
