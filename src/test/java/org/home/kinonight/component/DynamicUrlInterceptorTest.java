package org.home.kinonight.component;

import feign.RequestTemplate;
import org.home.kinonight.dto.TelegramCredentials;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamicUrlInterceptorTest {

    @InjectMocks
    DynamicUrlInterceptor testee;

    @Mock
    TelegramCredentials telegramCredentials;

    @Test
    void apply_ShouldUpdateUrl_WhenCalled() {
        // given
        String token = "/token";
        when(telegramCredentials.getTelegramApiToken()).thenReturn(token);
        // when
        RequestTemplate requestTemplate = new RequestTemplate();
        requestTemplate.uri("command");
        testee.apply(requestTemplate);
        // then
        verify(telegramCredentials).getTelegramApiToken();
        assertEquals(token + "/command", requestTemplate.url());
    }
}