package io.github.loncra.basic.service.message.server.resolver.support.sms;

import io.github.loncra.basic.service.commons.enumerate.CloudChannelEnum;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsSignResponseBody;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;

import java.util.List;
import java.util.Map;

public interface SmsSignResolver {

    /**
     * 获取短信渠道类型
     *
     * @return 短信渠道类型
     */
    CloudChannelEnum getType();

    /**
     * 查找签名
     * @param query 查询参数
     *
     * @return 签名集合
     */
    List<SmsSignResponseBody> find(Map<String, Object> query);

    /**
     * 查找签名分页
     *
     * @param request 分页请求
     * @param query 查询参数
     *
     * @return 分页对象
     */
    Page<SmsSignResponseBody> page(PageRequest request, Map<String, Object> query);

    /**
     * 获取签名
     *
     * @param id 签名 id
     *
     * @return 签名信息
     */
    Object get(String id);
}
