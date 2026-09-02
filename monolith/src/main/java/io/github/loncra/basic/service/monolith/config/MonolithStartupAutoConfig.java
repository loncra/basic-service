package io.github.loncra.basic.service.monolith.config;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.extensions.mysql.state.MysqlAgentStateStore;
import io.github.loncra.basic.service.auth.api.service.SystemUserServiceClient;
import io.github.loncra.basic.service.auth.api.service.web.SystemUserServiceWebClient;
import io.github.loncra.basic.service.commons.config.AlibabaCloudConfig;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.message.api.service.MessageServiceClient;
import io.github.loncra.basic.service.message.api.service.SocketServiceClient;
import io.github.loncra.basic.service.message.api.service.web.MessageServiceWebClient;
import io.github.loncra.basic.service.message.api.service.web.SocketServiceWebClient;
import io.github.loncra.basic.service.resource.api.service.AttachmentServiceClient;
import io.github.loncra.basic.service.resource.api.service.CaptchaServiceClient;
import io.github.loncra.basic.service.resource.api.service.DictionaryServiceClient;
import io.github.loncra.basic.service.resource.api.service.ResourceCaptchaVerificationService;
import io.github.loncra.basic.service.resource.api.service.web.AttachmentServiceWebClient;
import io.github.loncra.basic.service.resource.api.service.web.CaptchaServiceWebClient;
import io.github.loncra.basic.service.resource.api.service.web.DictionaryServiceWebClient;
import io.github.loncra.framework.captcha.CaptchaProperties;
import io.github.loncra.framework.captcha.storage.support.RedissonCaptchaStorageManager;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.generator.twitter.SnowflakeIdGenerator;
import io.github.loncra.framework.commons.tenant.SimpleTenantContext;
import io.github.loncra.framework.crypto.algorithm.Base64;
import io.github.loncra.framework.mybatis.plus.tenant.TenantLinePolicy;
import io.github.loncra.framework.socketio.api.SocketPrincipal;
import io.github.loncra.framework.spring.security.core.authentication.AccessTokenContextRepository;
import io.github.loncra.framework.spring.security.core.authentication.SpringSecurityTenantContext;
import io.github.loncra.framework.spring.security.core.authentication.config.AuthenticationProperties;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.support.AccessTokenAuditAuthenticationSuccessDetails;
import io.github.loncra.framework.spring.web.device.DeviceUtils;
import io.github.loncra.framework.spring.web.result.RestResponseBodyAdvice;
import jakarta.servlet.MultipartConfigElement;
import org.apache.catalina.connector.Connector;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.server.authentication.ServerHttpBasicAuthenticationConverter;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import javax.sql.DataSource;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import static io.github.loncra.basic.service.ai.server.config.AiStartupAutoConfig.AGENT_STATE_STORE_TABLE_NAME;

@Configuration
public class MonolithStartupAutoConfig {
    public static final String DEFAULT_TYPE = "feign";

    private static final DataSize MAX_UPLOAD_FILE = DataSize.ofMegabytes(50);
    private static final DataSize MAX_UPLOAD_REQUEST = DataSize.ofMegabytes(100);
    /** WebClient 编码 multipart 时默认仅约 256KB 内存缓冲，大文件需放大 */
    private static final int WEB_CLIENT_MAX_IN_MEMORY = 64 * 1024 * 1024;

    /**
     * 显式注册 multipart 限制，避免多模块 classpath 合并时仅依赖 yaml 不生效。
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(MAX_UPLOAD_FILE);
        factory.setMaxRequestSize(MAX_UPLOAD_REQUEST);
        return factory.createMultipartConfig();
    }

    /**
     * 提高 Tomcat 连接器对 POST 体大小的上限（默认约 2MB），否则会在 Spring 解析 multipart 之前拒绝请求。
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatUploadSizeCustomizer() {
        int maxBytes = (int) Math.min(MAX_UPLOAD_REQUEST.toBytes(), Integer.MAX_VALUE);
        return factory -> factory.addConnectorCustomizers((Connector connector) -> connector.setMaxPostSize(maxBytes));
    }

    @Bean
    public TenantLinePolicy tenantLinePolicy(AuthenticationProperties properties) {
        return tenantContext -> {
            if (tenantContext instanceof SpringSecurityTenantContext context) {
                return !ResourceSourceEnum.CONSOLE_SOURCE_VALUE.equals(context.getType());
            } else if (tenantContext instanceof SimpleTenantContext context) {
                return properties.getUsers().stream().map(SecurityProperties.User::getName).noneMatch(s -> s.equals(context.getId()));
            } else {
                return Objects.nonNull(tenantContext.getId());
            }
        };
    }

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator(MonolithAppConfig monolithAppConfig){
        return new SnowflakeIdGenerator(monolithAppConfig.getSnowflake());
    }

    @Bean
    public WebClient getWebClient(
            ServerProperties properties,
            AuthenticationProperties authenticationProperties
    ) {
        SecurityProperties.User user = authenticationProperties
                .getUsers()
                .stream()
                .filter(u -> u.getName().equals(DEFAULT_TYPE))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为:" + DEFAULT_TYPE + "的默认用户"));

        String token = user.getName() + CacheProperties.DEFAULT_SEPARATOR + user.getPassword();

        String base64 = Base64.encodeToString(token.getBytes(Charset.defaultCharset()));

        ExchangeFilterFunction requestFilter = (request, next) -> {
            ClientRequest.Builder modified = ClientRequest.from(request);
            modified.header(RestResponseBodyAdvice.DEFAULT_NOT_FORMAT_ATTR_NAME, Boolean.TRUE.toString());
            if (Objects.isNull(SecurityContextHolder.getContext())) {
                modified.header(HttpHeaders.AUTHORIZATION, ServerHttpBasicAuthenticationConverter.BASIC + base64);
            } else{
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
                    modified.header(HttpHeaders.AUTHORIZATION, ServerHttpBasicAuthenticationConverter.BASIC + base64);
                } else if (authentication instanceof AuditAuthenticationToken auditAuthenticationToken) {
                    String accessToken = getAccessTokenValue(auditAuthenticationToken);
                    if (StringUtils.isNotEmpty(accessToken)) {
                        modified.header(AccessTokenContextRepository.DEFAULT_ACCESS_TOKEN_HEADER_NAME, accessToken);
                        if (auditAuthenticationToken.getSecurityPrincipal() instanceof SocketPrincipal socketPrincipal) {
                            modified.header(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME, socketPrincipal.getDeviceIdentified());
                        }
                    } else {
                        modified.header(HttpHeaders.AUTHORIZATION, ServerHttpBasicAuthenticationConverter.BASIC + base64);
                    }
                } else if (authentication instanceof UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
                    String usernameToken = usernamePasswordAuthenticationToken.getName() + CacheProperties.DEFAULT_SEPARATOR + usernamePasswordAuthenticationToken.getCredentials().toString();

                    String usernameBase64 = Base64.encodeToString(usernameToken.getBytes(Charset.defaultCharset()));
                    modified.header(HttpHeaders.AUTHORIZATION, ServerHttpBasicAuthenticationConverter.BASIC + usernameBase64);
                }
            }

            return next.exchange(modified.build());
        };

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(WEB_CLIENT_MAX_IN_MEMORY))
                .build();

        return WebClient.builder()
                .baseUrl("http://localhost:" + properties.getPort())
                .filter(requestFilter)
                .exchangeStrategies(strategies)
                .build();
    }

    private String getAccessTokenValue(
            AuditAuthenticationToken auditAuthenticationToken
    ) {
        if (auditAuthenticationToken.getDetails() instanceof AccessTokenAuditAuthenticationSuccessDetails successDetails) {
            return successDetails.getToken().getValue();
        } else {
            return null;
        }
    }

    @Bean
    public CaptchaServiceClient captchaServiceClient(WebClient webClient) {
        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return factory.createClient(CaptchaServiceWebClient.class);
    }

    @Bean
    public DictionaryServiceClient dictionaryServiceClient(WebClient webClient) {
        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return factory.createClient(DictionaryServiceWebClient.class);
    }

    @Bean
    public AttachmentServiceClient attachmentServiceClient(WebClient webClient) {
        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return factory.createClient(AttachmentServiceWebClient.class);
    }

    @Bean
    public SystemUserServiceClient systemUserServiceClient(WebClient webClient) {
        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return factory.createClient(SystemUserServiceWebClient.class);
    }

    @Bean
    public MessageServiceClient messageServiceClient(WebClient webClient) {
        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return factory.createClient(MessageServiceWebClient.class);
    }

    @Bean
    public SocketServiceClient socketServiceClient(WebClient webClient) {
        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return factory.createClient(SocketServiceWebClient.class);
    }

    @Bean
    public ResourceCaptchaVerificationService resourceCaptchaVerificationService(CaptchaServiceClient captchaServiceClient) {
        return new ResourceCaptchaVerificationService(captchaServiceClient);
    }

    @Bean
    public RedissonCaptchaStorageManager captchaStorageManager(
            RedissonClient redissonClient,
            CaptchaProperties captchaProperties
    ) {
        return new RedissonCaptchaStorageManager(redissonClient, captchaProperties);
    }

    @Bean
    public Client alibabaClient(AlibabaCloudConfig alibabaCloudConfig) throws Exception {
        Config config = new Config()
                // 配置 AccessKey ID，请确保代码运行环境配置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID。
                .setAccessKeyId(alibabaCloudConfig.getSecretId())
                // 配置 AccessKey Secret，请确保代码运行环境配置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
                .setAccessKeySecret(alibabaCloudConfig.getSecretKey());

        // 配置 Endpoint
        config.endpoint = "dysmsapi.aliyuncs.com";

        return new Client(config);
    }

    /*@Bean
    public CaptchaVerificationInterceptor NopeCaptchaVerificationInterceptor() {
        return new CaptchaVerificationInterceptor() {
            @Override
            public boolean preVerify(
                    HttpServletRequest request,
                    HttpServletResponse response
            ) {
                return CaptchaVerificationInterceptor.super.preVerify(request, response);
            }
        };
    }*/

    @Bean
    public AgentStateStore agentStateStore(DataSource dataSource) throws SQLException {
        String catalog;
        try (Connection conn = dataSource.getConnection()) {
            catalog = conn.getCatalog();
        }
        return new MysqlAgentStateStore(
                dataSource,
                catalog,
                AGENT_STATE_STORE_TABLE_NAME,
                true
        );
    }

}
