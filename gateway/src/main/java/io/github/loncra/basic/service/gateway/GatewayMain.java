package io.github.loncra.basic.service.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 服务启动类
 *
 * @author maurice.chen
 */
@RequiredArgsConstructor
@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "io.github.loncra.basic.service.gateway")
public class GatewayMain  {

    public static void main(String[] args) {
        SpringApplication.run(GatewayMain.class, args);
    }
}