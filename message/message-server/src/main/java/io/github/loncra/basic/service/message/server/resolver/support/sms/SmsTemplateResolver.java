package io.github.loncra.basic.service.message.server.resolver.support.sms;

import io.github.loncra.basic.service.commons.enumerate.CloudChannelEnum;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsTemplateResponseBody;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;

import java.util.List;
import java.util.Map;

public interface SmsTemplateResolver {

    /**
     * 获取短信渠道类型
     *
     * @return 短信渠道类型
     */
    CloudChannelEnum getType();

    /**
     * 查找模版
     * @param query 查询参数
     *
     * @return 模版集合
     */
    List<SmsTemplateResponseBody> find(Map<String, Object> query);

    /**
     * 查找模版分页
     *
     * @param request 分页请求
     * @param query 查询参数
     *
     * @return 分页对象
     */
    Page<SmsTemplateResponseBody> page(PageRequest request,Map<String, Object> query);


    /**
     * 获取模版
     *
     * @param id 模版 id
     *
     * @return 模版信息
     */
    Object get(String id);
}
