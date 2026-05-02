package io.github.loncra.basic.service.commons.config;

import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.TimeProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 附件配置
 *
 * @author maurice.chen
 */
@Data
@Component
@EqualsAndHashCode
@NoArgsConstructor
@ConfigurationProperties("loncra.basic-service.commons.app.attachment")
public class AttachmentConfig {

    /**
     * 上传文件用于创建文件夹的参数名称
     */
    private String uploadFilePrefixParamName = "prefix";

    /**
     * 必须要带 prefix 参数的类型
     */
    private List<String> uploadPrefixType = Collections.singletonList("message");

    /**
     * 桶前缀
     */
    private String bucketPrefix = "loncra.basic-service.resource.";

    /**
     * 附件发送邮件字典代码
     */
    private String sendEmailDictionaryCode = "";

    /**
     * 多文件导出的文件名称
     */
    private String multiFileTitle = "多文件导出";

    /**
     * 响应结果集配置
     */
    private Result result = new Result();

    /**
     * 源文件名称
     */
    private String sourceField = "source";

    /**
     * 预览文件得过期时间
     */
    private TimeProperties presignedTime = TimeProperties.of(3650, TimeUnit.DAYS);

    private Integer uploadBlockSize = 5242880;

    private String uploadBlockSizeParamName = "uploadBlockSize";

    private String uploadIdParamName = "uploadId";

    private String chunkParamName = "chunk";

    private String accessDomain = "http://localhost:9000";

    /**
     * 创建分片上传的过期时间
     */
    private CacheProperties multipartUploadCache = CacheProperties.of(
            "loncra:basic-service:resource:attachment:multipart-upload",
            TimeProperties.of(1, TimeUnit.DAYS)
    );

    /**
     * 获取桶名称
     *
     * @param type 桶类型
     *
     * @return 桶名称
     */
    public String getBucketName(String type) {
        return Strings.CS.appendIfMissing(bucketPrefix, CastUtils.DOT) + Strings.CS.removeStart(type, CastUtils.DOT);
    }

    /**
     * 响应结果集配置
     *
     * @author maurice.chen
     */
    @Data
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class Result {

        /**
         * 上传文件要忽略的响应字段
         */
        private List<String> uploadResultIgnoreFields = Collections.singletonList("headers");

        /**
         * state 接口要忽略的响应字段
         */
        private List<String> statObjectIgnoreFields = Arrays.asList("legalHold", "headers");
    }
}
