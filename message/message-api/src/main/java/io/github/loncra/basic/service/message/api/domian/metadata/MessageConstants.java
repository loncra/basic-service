package io.github.loncra.basic.service.message.api.domian.metadata;

/**
 * 消息常量
 *
 * @author maurice..chen
 */
public interface MessageConstants {
    /**
     * 默认的消息类型 key 名称
     */
    String DEFAULT_MESSAGE_TYPE_KEY = "messageType";

    String DEFAULT_ARGS_FIELD_NAME = "args";

    String DEFAULT_ARGS_GENERATE_FIELD_NAME = "generate";

    String DEFAULT_MESSAGES_KEY = "messages";

    String DEFAULT_SITE_TYPE_VALUE = "site";

    String DEFAULT_SMS_TYPE_VALUE = "sms";

    String DEFAULT_EMAIL_TYPE_VALUE = "email";

    String DEFAULT_TITLE_KEY = "title";

    String DEFAULT_CONTENT_KEY = "content";

    String VARIABLES_FIELD = "variables";

    /**
     * 站内信常量
     */
    interface Site {
        String TO_USERS_FIELD = "toUsers";
        String IS_PUSHABLE_FIELD = "pushable";
        String LINK_META_FIELD = "link";
    }

    /**
     * 邮箱常量
     */
    interface Email {
        String TO_EMAILS_FIELD = "toEmails";
        String ATTACHMENT_LIST_FIELD = "attachmentList";
    }

    /**
     * 短信常量
     */
    interface Sms {
        String PHONE_NUMBERS_FIELD = "phoneNumbers";
        String TEMPLATE_CODE_FIELD = "templateCode";
        String SIGN_CODE_FIELD = "signCode";
        String CHANNEL_FIELD = "channel";
    }

}
