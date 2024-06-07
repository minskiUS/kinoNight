package org.home.kinonight.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = {"org.home.kinonight.feign"})
public class FeignConfig {
    }

