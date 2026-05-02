package io.github.loncra.basic.service.commons.domain.metadata;

import io.github.loncra.basic.service.commons.enumerate.ImportExportTypeEnum;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import io.github.loncra.framework.commons.id.BasicIdentification;
import io.github.loncra.framework.commons.retry.Retryable;
import io.github.loncra.framework.security.audit.AuditPrincipal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.AntPathMatcher;

import java.io.Serial;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 导出数据模型
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class ExportDataMetadata implements BasicIdentification<String>, ExecuteStatus.Body, Retryable, AuditPrincipal {

    @Serial
    private static final long serialVersionUID = 8006955473517765144L;

    /**
     * 主键 id
     */
    private String id = Objects.toString(UUID.randomUUID());

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
    private Instant creationTime = Instant.now();

    /**
     * 文件名称
     */
    private String filename;

    /**
     * 状态
     */
    private ExecuteStatus executeStatus = ExecuteStatus.Processing;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * 成功时间
     */
    private Instant successTime;

    /**
     * 最后导出时间
     */
    private Instant retryTime = Instant.now();

    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 3;

    /**
     * 导出类型
     */
    private ImportExportTypeEnum type;

    /**
     * 文件大小
     */
    private long size = 0;

    /**
     * 元数据信息
     */
    private Map<String, Object> metadata = new LinkedHashMap<>();

    /**
     * 用户信息
     */
    private String principal;

    public String toExportCacheName() {
        return getPrincipal()
                + CacheProperties.DEFAULT_SEPARATOR
                + getType().getValue()
                + CacheProperties.DEFAULT_SEPARATOR
                + getId();
    }

    public String toUploadFilename() {
        return getPrincipal() + AntPathMatcher.DEFAULT_PATH_SEPARATOR + getFilename();
    }
}
