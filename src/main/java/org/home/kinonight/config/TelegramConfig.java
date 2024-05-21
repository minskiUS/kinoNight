package org.home.kinonight.config;

import org.home.kinonight.component.KinoManagerBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    public BotSession botSession(TelegramBotsApi telegramBotsApi, KinoManagerBot kinoManagerBot) throws TelegramApiException {
        return telegramBotsApi.registerBot(kinoManagerBot);
    }

}
