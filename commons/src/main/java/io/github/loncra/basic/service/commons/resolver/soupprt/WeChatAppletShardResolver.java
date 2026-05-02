package io.github.loncra.basic.service.commons.resolver.soupprt;

import io.github.loncra.basic.service.commons.resolver.AppShardResolver;
import io.github.loncra.framework.wechat.service.WechatAppletService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * 微信小程序分享解析器实现
 *
 * @author maurice.chen
 */
@RequiredArgsConstructor
public class WeChatAppletShardResolver implements AppShardResolver {

    public static final String DEFAULT_TYPE = "WeChatApplet";

    public static final String DEFAULT_SCENE_PARAM_NAME = "scene";

    private final WechatAppletService wechatAppletService;

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }

    @Override
    public byte[] createQrCode(Map<String, Object> body) {
        return wechatAppletService.createAppletQrcode(body);
    }

    @Override
    public String getQueryParamName() {
        return DEFAULT_SCENE_PARAM_NAME;
    }

}
