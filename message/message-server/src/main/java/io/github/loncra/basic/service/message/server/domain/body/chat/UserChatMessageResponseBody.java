package io.github.loncra.basic.service.message.server.domain.body.chat;

import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserChatMessageResponseBody extends ParticipantMetadataMessageResponseBody {

    @Serial
    private static final long serialVersionUID = 444818783262032518L;
    /**
     * 可读数量
     */
    private Integer readableCount;
    /**
     * 已读数量
     */
    private Integer readCount;

    /**
     * 当前用户是否可读
     */
    private YesOrNo readable;
}
