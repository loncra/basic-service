package io.github.loncra.basic.service.message.server.domain.body.sms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.loncra.basic.service.message.server.domain.entity.BasicMessageEntity;
import io.github.loncra.framework.security.audit.AuditPrincipal;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 短信 body
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmsMessageBody extends BasicMessageEntity implements AuditPrincipal {

    @Serial
    private static final long serialVersionUID = -6678810630364920364L;

    @NotEmpty
    private String channel;

    /**
     * 收件方集合
     */
    @NotEmpty
    private List<String> phoneNumbers = new LinkedList<>();

    /**
     * 元数据信息
     */
    private Map<String, Object> metadata = new LinkedHashMap<>();

    /**
     * 操作人信息
     */
    private String principal;
}
