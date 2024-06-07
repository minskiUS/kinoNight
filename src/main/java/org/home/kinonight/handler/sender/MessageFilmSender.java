package org.home.kinonight.handler.sender;

import org.home.kinonight.dto.SetCommandRequest;
import org.home.kinonight.exception.AlreadyExistsException;
import org.home.kinonight.feign.TelegramClient;
import org.home.kinonight.model.Film;
import org.home.kinonight.model.UserState;
import org.home.kinonight.service.FilmService;
import org.home.kinonight.service.UserListService;
import org.home.kinonight.util.TelegramCommandsUtil;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;

import static org.home.kinonight.constants.Messages.*;
import static org.home.kinonight.factory.KeyboardFactory.addFilmDeleteFilmBackLogoutButtons;
import static org.home.kinonight.model.UserState.*;
import static org.home.kinonight.util.SendMessageUtil.sendMessage;
import static org.home.kinonight.util.SendMessageUtil.sendMessageWithKeyboard;

public class MessageFilmSender {
    private final SilentSender sender;
    private final Map<Long, UserState> chatStates;
    private final Map<Long, String> activeFilmList;
    private final UserListService userListService;
    private final FilmService filmService;
    private final TelegramClient telegramClient;

    public MessageFilmSender(SilentSender sender,
                             Map<Long, UserState> chatStates,
                             Map<Long, String> activeFilmList,
                             UserListService userListService,
                             FilmService filmService,
                             TelegramClient telegramClient) {
        this.sender = sender;
        this.chatStates = chatStates;
        this.activeFilmList = activeFilmList;
        this.userListService = userListService;
        this.filmService = filmService;
        this.telegramClient = telegramClient;
    }

    public void mainPage(Long chatId) {
        String activeList = activeFilmList.get(chatId);
        List<Film> filmsInActiveList = userListService.findByFilmList(chatId, activeList).getFilms();
        if (filmsInActiveList.isEmpty()) {
            sendMessage(chatId, ADD_FILM, sender);
            chatStates.put(chatId, AWAITING_FILM_NAME);
        } else {
            List<String> films = filmsInActiveList.stream().map(Film::getFilmName).toList();
            sendMessageWithKeyboard(chatId,
                    filmsInActiveList.size(),
                    FILM_COUNT,
                    films,
                    sender);
            sendMessage(chatId, CHOOSE_FILM, addFilmDeleteFilmBackLogoutButtons(), sender);
            SetCommandRequest setCommandRequest = TelegramCommandsUtil.setMyCommandsFilm(filmsInActiveList, chatId);
            telegramClient.setCommand(setCommandRequest);

            chatStates.put(chatId, AWAITING_FILM_CHOICE);
        }
    }

    public void filmToAddName(Long chatId) {
        sendMessage(chatId, ADD_FILM, sender);
        chatStates.put(chatId, AWAITING_FILM_NAME);
    }

    public void addFilm(long chatId, Message message) {
        try {
            filmService.save(message, activeFilmList);
            sendMessage(chatId, FILM_ADDED, sender);
        } catch (AlreadyExistsException e) {
            sendMessage(chatId, e.getMessage(), sender);
        }
        mainPage(chatId);
        chatStates.put(chatId, AWAITING_FILM_CHOICE);
    }

    public void filmToRemoveName(long chatId, String text) {
        sendMessage(chatId, text, sender);
        chatStates.put(chatId, AWAITING_FILM_TO_DELETE);
    }

    public void removeFilm(long chatId, Update update) {
        String filmName = update.getCallbackQuery().getData();
        try {
            String filmList = activeFilmList.get(chatId);
            filmService.delete(chatId, filmName, filmList);
            String filmRemovedMessage = String.format(FILM_REMOVED, filmName, filmList);
            sendMessage(chatId, filmRemovedMessage, sender);
        } catch (AlreadyExistsException e) {
            sendMessage(chatId, e.getMessage(), sender);
        }
        mainPage(chatId);
    }
}
