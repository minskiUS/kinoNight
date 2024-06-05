package org.home.kinonight.component;

import org.home.kinonight.handler.ResponseHandler;
import org.home.kinonight.model.TelegramCredentials;
import org.home.kinonight.service.FilmService;
import org.home.kinonight.service.UserListService;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.home.kinonight.constants.Buttons.*;
import static org.home.kinonight.constants.Messages.FILM_TO_REMOVE;

@Component
public class KinoManagerBot extends AbilityBot {

    private final ResponseHandler responseHandler;

    public KinoManagerBot(TelegramCredentials telegramSecret,
                          UserListService userListService,
                          FilmService filmService) {
        super(telegramSecret.getSecret(), telegramSecret.getUserName());

        responseHandler = new ResponseHandler(silent, db, userListService, filmService);
    }

    @Override
    public long creatorId() {
        return 1L;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message != null && START.equalsIgnoreCase(message.getText())) {
            responseHandler.replyToStart(message);
        } else if (message != null && LOGOUT.equalsIgnoreCase(message.getText())) {
            responseHandler.deleteChat(message.getChatId());
        } else if (message != null && ADD_FILM.equalsIgnoreCase(message.getText())) {
            responseHandler.addFilm(update);
        } else if (message != null && DELETE_FILM.equalsIgnoreCase(message.getText())) {
            responseHandler.filmToRemoveName(message.getChatId(), FILM_TO_REMOVE);
        } else if (message != null && BACK.equalsIgnoreCase(message.getText())) {
            responseHandler.mainPage(message.getChatId());
        } else {
            responseHandler.replyToUpdate(update);
        }
    }
}
