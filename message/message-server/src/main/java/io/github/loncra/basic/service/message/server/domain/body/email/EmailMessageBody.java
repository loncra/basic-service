package io.github.loncra.basic.service.message.server.domain.body.email;

import io.github.loncra.basic.service.commons.domain.AttachmentMessage;
import io.github.loncra.basic.service.message.server.domain.entity.BasicMessageEntity;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.security.audit.AuditPrincipal;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 邮件消息 body
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmailMessageBody extends BasicMessageEntity implements AttachmentMessage, AuditPrincipal {

    @Serial
    private static final long serialVersionUID = -1367698344075208239L;

    public static final String EMAIL_REGEX = "attachment";

    /**
     * 标题
     */
    private String title;

    /**
     * 操作人信息
     */
    private String principal;

    /**
     * 收件方集合
     */
    @NotEmpty
    @Pattern(regexp = "^(CONSOLE|CONSOLE:\\d+:[^:]+|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$")
    private List<String> toEmails = new LinkedList<>();

    /**
     * 附件
     */
    private List<ObjectWriteResult> attachmentList = new ArrayList<>();

}
