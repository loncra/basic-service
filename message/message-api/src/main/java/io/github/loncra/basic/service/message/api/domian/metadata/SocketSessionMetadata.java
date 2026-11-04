package io.github.loncra.basic.service.message.api.domian.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SocketSessionMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String EXECUTE_SHELL_METADATA_NAME = "executeShell";

    private boolean online;

    private String principal;

    private Map<String, Object> metadata;
}
