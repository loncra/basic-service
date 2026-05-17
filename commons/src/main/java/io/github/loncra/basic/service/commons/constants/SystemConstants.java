package io.github.loncra.basic.service.commons.constants;

import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.minio.Bucket;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 系统常量类
 *
 * @author maurice.chen
 */
public interface SystemConstants {

    /**
     * 默认 rabbitmq 交换机名称
     */
    String RABBIT_EXCHANGE = "loncra.basic.service.exchange";

    /**
     * 默认 rabbitmq 广播交换机名称
     */
    String RABBIT_FANOUT_EXCHANGE = "loncra.basic.service.fanout.exchange";

    /**
     * 权限系统名称
     */
    String SYS_AUTH_NAME = "auth-server";

    /**
     * 网关服务名称
     */
    String SYS_GATEWAY_NAME = "gateway";

    /**
     * 资源系统名称
     */
    String SYS_RESOURCE_NAME = "resource-server";

    /**
     * 消息系统名称
     */
    String SYS_MESSAGE_NAME = "message-server";

    /**
     * ai 系统名称
     */
    String SYS_AI_NAME = "ai-server";

    /**
     * ai 系统名称
     */
    String SYS_VIDEO_ASSEMBLY = "video-assembly-server";

    /**
     * socket 系统名称
     */
    String SYS_SOCKET_SERVER_NAME = "netty-socket-server";


    /**
     * 消息系统 rabbitmq 交换机队列
     */
    String SYS_MESSAGE_RABBITMQ_EXCHANGE = RABBIT_EXCHANGE + CastUtils.DOT + SYS_MESSAGE_NAME;

    /**
     * 认证系统 rabbitmq 交换机队列
     */
    String SYS_AUTH_RABBITMQ_EXCHANGE = RABBIT_EXCHANGE + CastUtils.DOT + SYS_AUTH_NAME;

    /**
     * ai 系统 rabbitmq 交换机队列
     */
    String SYS_AI_RABBITMQ_EXCHANGE = RABBIT_EXCHANGE + CastUtils.DOT + SYS_AI_NAME;

    /**
     * 资源系统 rabbitmq 交换机队列
     */
    String SYS_RESOURCE_RABBITMQ_EXCHANGE = RABBIT_EXCHANGE + CastUtils.DOT + SYS_RESOURCE_NAME;

    /**
     * 资源系统 rabbitmq 交换机队列
     */
    String SYS_VIDEO_ASSEMBLY_RABBITMQ_EXCHANGE = RABBIT_EXCHANGE + CastUtils.DOT + SYS_VIDEO_ASSEMBLY;

    /**
     * 开放平台商户创建广播交换机
     */
    String RESOURCE_OPEN_PLATFORM_MERCHANT_SAVE_FANOUT_EXCHANGE = RABBIT_FANOUT_EXCHANGE + CastUtils.DOT + SYS_RESOURCE_NAME + CastUtils.DOT + "open.platform.merchant.create";

    /**
     * 开放平台商户删除广播交换机
     */
    String RESOURCE_OPEN_PLATFORM_MERCHANT_DELETE_FANOUT_EXCHANGE = RABBIT_FANOUT_EXCHANGE + CastUtils.DOT + SYS_RESOURCE_NAME + CastUtils.DOT + "open.platform.merchant.delete";

    String RESOURCE_ATTACHMENT_FANOUT_EXCHANGE = RABBIT_FANOUT_EXCHANGE + CastUtils.DOT + SYS_RESOURCE_NAME + CastUtils.DOT + "attachment.delete";

    String YEAR_FORMATTER_PATTERN = "yyyy";

    String TRACE_ID_FIELD_NAME = "traceId";

    /**
     * 电话号码正则表达式(""值不做校验)
     */
    String PHONE_NUMBER_REGULAR_EXPRESSION = "^$|^1[3456789]\\d{9}$";

    /**
     * 邮箱验证正则表达式
     */
    String EMAIL_REGULAR_EXPRESSION = "^(?:$|[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7})$";

    /**
     * 身份证号验证正则表达式
     * 前6位地区码（排除不合法区划）
     * 年（1800-2099）
     * 月（01-12）
     * 日（01-31）
     * 特殊月份日期（如04-30）
     * 非闰年2月（01-28）
     * 顺序码（3位）
     * 校验码（0-9/X/x）
     * 15位旧身份证
     */
    String ID_CARD_REGULAR_EXPRESSION = "^([1-6][1-9]|50)\\d{4}"
            + "(18|19|20)\\d{2}"
            + "((0[1-9]|1[0-2])"
            + "(0[1-9]|[12]\\d|3[01])"
            + "|(0[13-9]|1[0-2])(29|30)"
            + "|02(0[1-9]|1\\d|2[0-8]))"
            + "\\d{3}"
            + "([0-9Xx])"
            + "|([1-6][1-9]|50)\\d{4}\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}$"; //

    String APP_ID_FIELD_NAME = "appId";

    String APP_KEY_FIELD_NAME = "appKey";

    /**
     * 验证码 token 名称
     */
    String CAPTCHA_TOKEN_NAME = "captchaToken";

    String CAPTCHA_TOKEN_PARAM_NAME = "tokenParamName";

    String IP_ADDRESS_NAME = "ipAddress";

    String MAC_ADDRESS_NAME = "macAddress";

    String STATUS_TABLE_FIELD_NAME = "status";

    String PRINCIPAL_FIELD_NAME = "principal";

    String ACCESS_TOKEN_FIELD_NAME = "accessToken";

    String ENABLED_FIELD_NAME = "enabled";

    String AUDIT_EVENT_AUTHENTICATION_TYPE_NAME = "AUTHENTICATION_SUCCESS";

    String ES_OPERATION_DATE_TARGET_NAME = "data.operationDataTrace.target";

    String ES_OPERATION_DATE_ENTITY_ID_NAME = "data.operationDataTrace.entityId";

    String IDENTITY_FIELD_NAME = "identity";

    String AUTHENTICATED_FIELD_NAME = "authenticated";

    String PROMO_CODE_FIELD_NAME = "promoCode";

    String BASE64_FORMAT_PARMA_NAME = "base64Format";

    String APPEND_PARAM_FIELD_NAME = "appendParam";

    String EXPIRATION_TIME_FIELD_NAME = "expirationTime";

    String DOWNLOAD_FIELD_NAME = "download";

    String ATTACHMENT_FIELD_NAME = "attachment";

    String ROLE_FIELD_NAME = "role";

    /**
     * 导出的桶信息
     */
    Bucket EXPORT_BUCKET = Bucket.of("loncra.basic.service.resource.temp");

    CacheProperties USER_IMPORT_CACHE = CacheProperties.of("loncra:basic-service:resources:user:import", TimeProperties.of(7, TimeUnit.DAYS));

    CacheProperties USER_EXPORT_CACHE = CacheProperties.of("loncra:basic-service:resources:user:export:", TimeProperties.of(7, TimeUnit.DAYS));

    ParameterizedTypeReference<Map<String, Object>> MAP_REFERENCE = new ParameterizedTypeReference<>() {
    };

    String RUNTIME_MODE_KEY = "runtimeMode";

    String QUERY_KEY = "query";

    String EXCEL_SUFFIX_NAME = ".xlsx";

    String SORT_FIELD = "sort";
}
