package io.github.loncra.basic.service.message.api.domian.metadata;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
public class SocketSessionMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String EXECUTE_SHELL_METADATA_NAME = "executeShell";

    private boolean online;

    private String principal;

    private Map<String, Object> metadata;

    public static SocketSessionMetadata of(boolean online, String principal, Map<String, Object> metadata) {
        SocketSessionMetadata metadataBody = new SocketSessionMetadata();
        metadataBody.setOnline(online);
        metadataBody.setPrincipal(principal);
        metadataBody.setMetadata(metadata);
        return metadataBody;
    }
}
