package io.github.loncra.basic.service.message.server.domain.body.site;

import io.github.loncra.basic.service.commons.domain.AttachmentMessage;
import io.github.loncra.basic.service.message.api.enumerate.SiteMessagePushableChannelEnum;
import io.github.loncra.basic.service.message.server.domain.entity.BasicMessageEntity;
import io.github.loncra.framework.commons.annotation.JsonCollectionGenericType;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.security.audit.AuditPrincipal;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.*;

/**
 * 站内信消息 body
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SiteMessageBody extends BasicMessageEntity implements AttachmentMessage {

    @Serial
    private static final long serialVersionUID = 4341146261560926962L;

    /**
     * 标题
     */
    private String title;

    /**
     * 推送渠道
     */
    @JsonCollectionGenericType(SiteMessagePushableChannelEnum.class)
    private List<SiteMessagePushableChannelEnum> channels = new LinkedList<>();

    /**
     * 接收方用户
     */
    @NotEmpty
    private List<String> toUsers = new LinkedList<>();

    /**
     * 附件
     */
    private List<ObjectWriteResult> attachmentList = new ArrayList<>();

    /**
     * 是否推送消息：0.否，1.是
     */
    @NotNull
    private YesOrNo pushable;

    /**
     * 元数据信息
     */
    private Map<String, Object> metadata = new LinkedHashMap<>();

    /**
     * 封面
     */
    private ObjectWriteResult cover;
}
