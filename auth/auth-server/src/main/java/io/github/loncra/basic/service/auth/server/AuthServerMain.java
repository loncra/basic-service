package io.github.loncra.basic.service.auth.server;


import io.github.loncra.basic.service.commons.config.CommonsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * 服务启动类
 *
 * @author maurice.chen
 */
@EnableScheduling
@EnableWebSecurity
@EnableDiscoveryClient
@EnableMethodSecurity(securedEnabled = true)
@EnableConfigurationProperties(CommonsConfig.class)
@EnableFeignClients({
        "io.github.loncra.basic.service.auth.api.service",
        "io.github.loncra.basic.service.resource.api.service"
})
@SpringBootApplication(
        scanBasePackages = {
                "io.github.loncra.basic.service.auth.server",
                "io.github.loncra.basic.service.commons"
        }
)
public class AuthServerMain {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerMain.class, args);
    }
}
