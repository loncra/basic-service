package io.github.loncra.basic.service.auth.server.resolver;

import io.github.loncra.basic.service.commons.enumerate.CloudChannelEnum;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.MultiValueMap;

/**
 * 手机号码一键认证解析器
 *
 * @author maruice.chen
 */
public interface PhoneNumberAuthenticationResolver {

    /**
     * 获取手机号码
     *
     * @param request http 请求信息
     *
     * @return 手机号码
     */
    String getPhoneNumber(HttpServletRequest request);

    /**
     * 获取手机号码
     *
     * @param request http 请求参数
     *
     * @return 手机号码
     */
    String getPhoneNumber(MultiValueMap<String, String> request);

    /**
     * 验证码手机号码
     *
     * @param params http 请求信息
     *
     * @return true 成功，false 失败
     */
    boolean verifyPhoneNumber(MultiValueMap<String, String> params);

    /**
     * 获取手机号码认证类型
     *
     * @return 类型
     */
    CloudChannelEnum getType();

}
