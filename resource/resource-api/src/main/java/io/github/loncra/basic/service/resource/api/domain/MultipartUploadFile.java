package io.github.loncra.basic.service.resource.api.domain;

import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 服务端文件上传实体
 *
 * @author maurice.chen
 */
@Data
public class MultipartUploadFile implements MultipartFile {

    private final String name;

    private final String originalFilename;

    @Nullable
    private final String contentType;

    private final byte[] content;


    /**
     * Create a new MultipartUploadFile with the given content.
     *
     * @param name    the name of the file
     * @param content the content of the file
     */
    public MultipartUploadFile(
            String name,
            @Nullable
            byte[] content
    ) {
        this(name, "", null, content);
    }

    public MultipartUploadFile(MultipartFile file) throws IOException {
        this(file.getName(), file.getOriginalFilename(), file.getContentType(), file.getBytes());
    }

    /**
     * Create a new MultipartUploadFile with the given content.
     *
     * @param name          the name of the file
     * @param contentStream the content of the file as stream
     *
     * @throws IOException if reading from the stream failed
     */
    public MultipartUploadFile(
            String name,
            InputStream contentStream
    ) throws IOException {
        this(name, "", null, FileCopyUtils.copyToByteArray(contentStream));
    }

    /**
     * Create a new MultipartUploadFile with the given content.
     *
     * @param name             the name of the file
     * @param originalFilename the original filename (as on the client's machine)
     * @param contentType      the content type (if known)
     * @param content          the content of the file
     */
    public MultipartUploadFile(
            String name,
            @Nullable
            String originalFilename,
            @Nullable
            String contentType,
            @Nullable
            byte[] content
    ) {

        Assert.hasLength(name, "Name must not be empty");
        this.name = name;
        // 空 filename 时，HTTP 客户端会写出 filename=""，部分容器把该 part 当普通字段，内容进 getParameterMap 而非 MultipartFile
        this.originalFilename = StringUtils.hasText(originalFilename) ? originalFilename : name;
        this.contentType = contentType;
        this.content = (content != null ? content : new byte[0]);
    }

    /**
     * Create a new MultipartUploadFile with the given content.
     *
     * @param name             the name of the file
     * @param originalFilename the original filename (as on the client's machine)
     * @param contentType      the content type (if known)
     * @param contentStream    the content of the file as stream
     *
     * @throws IOException if reading from the stream failed
     */
    public MultipartUploadFile(
            String name,
            @Nullable
            String originalFilename,
            @Nullable
            String contentType,
            InputStream contentStream
    ) throws IOException {

        this(name, originalFilename, contentType, FileCopyUtils.copyToByteArray(contentStream));
    }

    @Override
    public boolean isEmpty() {
        return (this.content.length == 0);
    }

    @Override
    public long getSize() {
        return this.content.length;
    }

    @Override
    public byte @NonNull [] getBytes() {
        return this.content;
    }

    @Override
    @NonNull
    public InputStream getInputStream() {
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public void transferTo(
            @NonNull
            File dest
    ) throws IOException, IllegalStateException {
        FileCopyUtils.copy(this.content, dest);
    }
}
