package org.home.kinonight.component;

import org.home.kinonight.dto.TelegramCredentials;
import org.home.kinonight.feign.TelegramClient;
import org.home.kinonight.handler.ResponseHandler;
import org.home.kinonight.model.ExceptionDetails;
import org.home.kinonight.service.CommandRequestService;
import org.home.kinonight.service.FilmService;
import org.home.kinonight.service.UserListService;
import org.home.kinonight.util.SendMessageUtil;
import org.springframework.core.env.Environment;
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
                          FilmService filmService,
                          CommandRequestService commandRequestService,
                          TelegramClient telegramClient,
                          Environment environment) {
        super(telegramSecret.getSecret(), telegramSecret.getUserName());

        responseHandler = new ResponseHandler(silent, db, userListService, filmService, commandRequestService, telegramClient, environment);
    }

    @Override
    public long creatorId() {
        return 1L;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message == null) {
            responseHandler.replyToUpdate(update);
            return;
        }
        message.setText(message.getText().toLowerCase());
        // TODO fix later

        if (START.equalsIgnoreCase(message.getText()) || ANOTHER_START.equalsIgnoreCase(message.getText())) {
            responseHandler.replyToStart(message);
        } else if (LOGOUT.equalsIgnoreCase(message.getText())) {
            responseHandler.deleteChat(message.getChatId());
        } else if (ADD_FILM.equalsIgnoreCase(message.getText())) {
            responseHandler.addFilm(message.getChatId());
        } else if (REMOVE_FILM.equalsIgnoreCase(message.getText())) {
            responseHandler.removeFilm(message.getChatId(), FILM_TO_REMOVE);
        } else if (CREATE_NEW_LIST.equalsIgnoreCase(message.getText())) {
            responseHandler.createFilmList(message);
        } else if (BACK.equalsIgnoreCase(message.getText())) {
            responseHandler.filmListsMainPage(message.getChatId());
        } else if (MARK_AS_WATCHED.equalsIgnoreCase(message.getText())) {
            responseHandler.markAsWatched(message.getChatId());
        } else if (REMOVE_USER_LIST.equalsIgnoreCase(message.getText())) {
            responseHandler.removeFilmList(message.getChatId());
        } else {
            responseHandler.replyToUpdate(update);
        }
    }

    public void sendExceptionMessageToUser(ExceptionDetails exceptionDetails) {
        SendMessageUtil.sendMessage(exceptionDetails.getChatId(), exceptionDetails.getMessage(), silent);
    }
}
