package io.github.loncra.basic.service.ai.server.resolver.filesystem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.agentscope.harness.agent.filesystem.remote.store.BaseStore;
import io.agentscope.harness.agent.filesystem.remote.store.StoreItem;
import io.github.loncra.basic.service.resource.api.domain.MultipartUploadFile;
import io.github.loncra.basic.service.resource.api.service.AttachmentServiceClient;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.minio.FileObject;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;

import java.util.*;

/**
 * {@link BaseStore} 的资源服务实现：对象内容经 resource-server 统一 API 落对象存储。
 *
 * <p>映射规则：{@code (namespace, key)} → 对象名 {@code objectPrefix + namespace.join(AntPathMatcher.DEFAULT_PATH_SEPARATOR) + AntPathMatcher.DEFAULT_PATH_SEPARATOR + key}。
 * value 信封 {@code {"version": n, "value": {...}}} 由本类维护版本号，供
 * {@link BaseStore#putIfVersion} 做乐观并发控制。
 *
 * <p>已知限制：{@code putIfVersion} 为读-校验-写，跨副本非严格原子（对象存储 ETag CAS 可后续增强）。
 */
public class ResourceServerBaseStore implements BaseStore {

    private final AttachmentServiceClient attachmentServiceClient;

    private final String bucket;

    private final String objectPrefix;

    public ResourceServerBaseStore(
            AttachmentServiceClient attachmentServiceClient,
            String bucket,
            String objectPrefix
    ) {
        this.attachmentServiceClient = attachmentServiceClient;
        this.bucket = bucket;
        this.objectPrefix = objectPrefix;
    }

    @Override
    public StoreItem get(List<String> namespace, String key) {
        byte[] bytes = attachmentServiceClient.getAttachmentFile(bucket, objectKey(namespace, key), true);
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return deserialize(key, bytes);
    }

    @Override
    public void put(List<String> namespace, String key, Map<String, Object> value) {
        StoreItem existing = get(namespace, key);
        long nextVersion = existing != null ? existing.version() + 1 : 1L;
        uploadEnvelope(namespace, key, nextVersion, value);
    }

    @Override
    public boolean putIfVersion(
            List<String> namespace,
            String key,
            Map<String, Object> value,
            long expectedVersion
    ) {
        StoreItem existing = get(namespace, key);
        long currentVersion = existing != null ? existing.version() : 0L;
        if (currentVersion != expectedVersion) {
            return false;
        }
        uploadEnvelope(namespace, key, currentVersion + 1, value);
        return true;
    }

    @Override
    public List<StoreItem> search(List<String> namespace, int limit, int offset) {
        String prefix = objectPrefix(namespace);
        List<ObjectWriteResult> objects = attachmentServiceClient.findAttachment(bucket, prefix, true, true);
        if (objects == null || objects.isEmpty()) {
            return List.of();
        }

        List<StoreItem> items = new ArrayList<>();
        for (ObjectWriteResult object : objects) {
            String objectName = object.getObjectName();
            if (objectName == null || !objectName.startsWith(prefix)) {
                continue;
            }
            byte[] bytes = attachmentServiceClient.getAttachmentFile(bucket, objectName, true);
            if (bytes == null || bytes.length == 0) {
                continue;
            }
            StoreItem item = deserialize(AntPathMatcher.DEFAULT_PATH_SEPARATOR + objectName.substring(prefix.length()), bytes);
            if (item != null) {
                items.add(item);
            }
        }

        items.sort(Comparator.comparing(StoreItem::key));
        int start = Math.min(offset, items.size());
        int end = Math.min(start + limit, items.size());
        return items.subList(start, end);
    }

    @Override
    public void delete(List<String> namespace, String key) {
        attachmentServiceClient.deleteAttachment(
                List.of(FileObject.of(bucket, objectKey(namespace, key))),
                Map.of()
        );
    }

    private String objectPrefix(List<String> namespace) {
        return objectPrefix + String.join(AntPathMatcher.DEFAULT_PATH_SEPARATOR, namespace) + AntPathMatcher.DEFAULT_PATH_SEPARATOR;
    }

    private String objectKey(List<String> namespace, String key) {
        String normalized = key.startsWith(AntPathMatcher.DEFAULT_PATH_SEPARATOR) ? key.substring(1) : key;
        return objectPrefix(namespace) + normalized;
    }

    private void uploadEnvelope(
            List<String> namespace,
            String key,
            long version,
            Map<String, Object> value
    ) {
        String objectName = objectKey(namespace, key);
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("version", version);
            envelope.put("value", value);
            byte[] bytes = CastUtils.getObjectMapper().writeValueAsBytes(envelope);
            attachmentServiceClient.uploadAttachmentFile(
                    new MultipartUploadFile(objectName, objectName, MediaType.APPLICATION_JSON_VALUE, bytes),
                    bucket,
                    Map.of()
            );
        } catch (Exception e) {
            throw new IllegalStateException("写入资源服务对象失败: " + objectName, e);
        }
    }

    private StoreItem deserialize(String key, byte[] bytes) {
        try {
            JsonNode node = CastUtils.getObjectMapper().readTree(bytes);
            long version = node.path("version").asLong(0L);
            Map<String, Object> value = CastUtils.convertValue(node.path("value"), new TypeReference<Map<String, Object>>() {
            });
            return new StoreItem(key, value, version);
        } catch (Exception e) {
            return null;
        }
    }
}
