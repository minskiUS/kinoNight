package org.home.kinonight.component;

import org.home.kinonight.handler.ResponseHandler;
import org.home.kinonight.model.TelegramCredentials;
import org.home.kinonight.service.UserListService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.home.kinonight.constants.Buttons.LOGOUT;
import static org.home.kinonight.constants.Buttons.START;

@Component
public class KinoManagerBot extends AbilityBot {

    private final ResponseHandler responseHandler;

    public KinoManagerBot(TelegramCredentials telegramSecret,
                          UserListService userListService,
                          Environment environment) {
        super(telegramSecret.getSecret(), telegramSecret.getUserName());

        responseHandler = new ResponseHandler(silent, db, userListService, environment);
    }

    @Override
    public long creatorId() {
        return 1L;
    }

    /*public Ability startBot() {
        return Ability
                .builder()
                .name("start")
                .info(Messages.START_DESCRIPTION)
                .locality(USER)
                .privacy(PUBLIC)
                .action(responseHandler::replyToStart)
                .build();
    }*/

    /*public Reply replyToButtons() {
        BiConsumer<BaseAbilityBot, Update> action = (abilityBot, upd) -> responseHandler.replyToButtons(getChatId(upd), upd);
        return Reply.of(action, Flag.TEXT,upd -> responseHandler.userIsActive(getChatId(upd)));
    }*/

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (START.equalsIgnoreCase(message.getText())) {
            responseHandler.replyToStart(message);
        } else if (LOGOUT.equalsIgnoreCase(message.getText())) {
            responseHandler.deleteChat(message.getChatId());
        } else {
            responseHandler.replyToUpdate(update);
        }
    }
}
