package org.home.kinonight.component;

import org.home.kinonight.constants.Constants;
import org.home.kinonight.handler.ResponseHandler;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Component
public class MyAbilityBot extends AbilityBot {

    private final ResponseHandler responseHandler;

    public MyAbilityBot(Environment environment) {
        super(environment.getProperty("telegram.bot.secret"), environment.getProperty("telegram.bot.username"));
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
