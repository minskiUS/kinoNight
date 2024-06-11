package org.home.kinonight.config;

import org.home.kinonight.component.DynamicUrlInterceptor;
import org.home.kinonight.dto.TelegramCredentials;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = {"org.home.kinonight.feign"})
public class FeignConfig {

    @Bean
    public DynamicUrlInterceptor dynamicUrlInterceptor(TelegramCredentials telegramCredentials) {
        return new DynamicUrlInterceptor(telegramCredentials);
    }
}

