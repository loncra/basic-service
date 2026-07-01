package io.github.loncra.basic.service.message.server.domain.metadata.chat;

import com.baomidou.mybatisplus.annotation.TableField;
import io.github.loncra.basic.service.message.server.enumerate.chat.UserChatParticipantTypeEnum;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.security.audit.AuditPrincipal;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class UserChatParticipantMetadata implements AuditPrincipal{

    /**
     * 类型
     */
    private UserChatParticipantTypeEnum type;

    /**
     * 参与者
     */
    private String principal;

    /**
     * 元数据信息
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> metadata = new LinkedHashMap<>();
}
