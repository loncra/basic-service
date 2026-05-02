package io.github.loncra.basic.service.commons.resolver;

import java.util.Map;

/**
 * app 分享解析器
 *
 * @author maurice.chen
 */
public interface AppShardResolver {

    /**
     * 获取分型类型
     *
     * @return 类型
     */
    String getType();

    /**
     * 创建二维码
     *
     * @param body 请求体
     *
     * @return 图片信息
     */
    byte[] createQrCode(Map<String, Object> body);

    /**
     * 获取分享页带参数的参数名称
     *
     * @return 分享页带参数的参数名称
     */
    String getQueryParamName();
}
