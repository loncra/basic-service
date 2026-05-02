package io.github.loncra.basic.service.monolith;

import io.github.loncra.basic.service.commons.config.CommonsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * 单体运行时
 *
 * @author maurice.chen
 */
@EnableScheduling
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@EnableConfigurationProperties(CommonsConfig.class)
@SpringBootApplication(scanBasePackages = "io.github.loncra.basic.service")
public class MonolithMain {

    public static void main(String[] args) {
        SpringApplication.run(MonolithMain.class, args);
    }

}

