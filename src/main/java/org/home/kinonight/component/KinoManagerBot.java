package org.home.kinonight.component;

import org.home.kinonight.constants.Constants;
import org.home.kinonight.handler.ResponseHandler;
import org.home.kinonight.model.TelegramCredentials;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Component
public class KinoManagerBot extends AbilityBot {

    private final ResponseHandler responseHandler;

    public KinoManagerBot(TelegramCredentials telegramSecret) {
        super(telegramSecret.getSecret(), telegramSecret.getUserName());

        responseHandler = new ResponseHandler(silent, db);
    }

    @Override
    public long creatorId() {
        return 1L;
    }

    public Ability startBot() {
        return Ability
                .builder()
                .name("start")
                .info(Constants.START_DESCRIPTION)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> responseHandler.replyToStart(ctx.chatId()))
                .build();
    }
}
