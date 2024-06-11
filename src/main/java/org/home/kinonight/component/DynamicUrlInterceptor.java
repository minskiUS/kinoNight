package org.home.kinonight.component;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.AllArgsConstructor;
import org.home.kinonight.dto.TelegramCredentials;

@AllArgsConstructor
public class DynamicUrlInterceptor implements RequestInterceptor {

    private TelegramCredentials telegramCredentials;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String telegramApiToken = telegramCredentials.getTelegramApiToken();
        String command = requestTemplate.url();
        requestTemplate.uri(telegramApiToken).uri(command, true);
    }
}
