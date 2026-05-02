package io.github.loncra.basic.service.message.server.enumerate;

import io.github.loncra.basic.service.message.server.domain.body.email.EmailMessageBody;
import io.github.loncra.basic.service.message.server.domain.body.site.SiteMessageBody;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsMessageBody;
import io.github.loncra.basic.service.message.server.domain.entity.BasicMessageEntity;
import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import io.github.loncra.framework.commons.exception.SystemException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 附件类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum BatchMessageTypeEnum implements NameValueEnum<Integer> {

    /**
     * 站内信
     */
    SITE("站内信", 10, SiteMessageBody.class),
    /**
     * 邮件
     */
    EMAIL("邮件", 20, EmailMessageBody.class),
    /**
     * 短信
     */
    SMS("短信", 30, SmsMessageBody.class),
    ;

    /**
     * 名称
     */
    private final String name;

    /**
     * 值
     */
    private final Integer value;

    /**
     * 类类型
     */
    private final Class<? extends BasicMessageEntity> type;

    /**
     * 通过类类型获取枚举内容
     *
     * @param type 类类型
     * @return 实际枚举只
     */
    public static BatchMessageTypeEnum valueOf(Class<? extends BasicMessageEntity> type) {

        for (BatchMessageTypeEnum t : BatchMessageTypeEnum.values()) {
            if (t.getType().equals(type)) {
                return t;
            }
        }

        throw new SystemException("找不到类型为 [" + type + "] 的枚举内容");
    }
}
