package org.home.kinonight.handler;

import org.home.kinonight.exception.AlreadyExistsException;
import org.home.kinonight.factory.KeyboardFactory;
import org.home.kinonight.model.Film;
import org.home.kinonight.model.UserList;
import org.home.kinonight.model.UserState;
import org.home.kinonight.service.FilmService;
import org.home.kinonight.service.UserListService;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.home.kinonight.constants.Buttons.CREATE_NEW_LIST;
import static org.home.kinonight.constants.Messages.*;
import static org.home.kinonight.model.UserState.*;

public class ResponseHandler {
    private final SilentSender sender;
    private final Map<Long, UserState> chatStates;
    private final Map<Long, String> activeFilmList;
    private final UserListService userListService;
    private final FilmService filmService;

    public ResponseHandler(SilentSender sender,
                           DBContext db,
                           UserListService userListService,
                           FilmService filmService) {
        this.sender = sender;
        chatStates = db.getMap(CHAT_STATES);
        this.userListService = userListService;
        this.filmService = filmService;
        activeFilmList = new ConcurrentHashMap<>();
    }

    public void replyToStart(Message message) {
        Long chatId = message.getChatId();
        String firstName = message.getFrom().getFirstName();
        String lastName = message.getFrom().getFirstName();
        String welcomeMessage = String.format(START_TEXT, firstName, lastName);
        sendMessage(chatId, welcomeMessage);

        mainPage(chatId);
    }

    public void mainPage(Long chatId) {
        List<UserList> byUserId = userListService.findByUserId(chatId);
        if (byUserId.isEmpty()) {
            sendMessage(chatId, CREATE_FILM_LIST);
            chatStates.put(chatId, AWAITING_FILM_LIST_NAME);
        } else {
            int size = byUserId.size();
            String count = String.format(LIST_COUNT, size);
            List<String> filmList = byUserId.stream()
                    .map(UserList::getListName)
                    .toList();
            InlineKeyboardMarkup inlineKeyboardMarkup = KeyboardFactory.chooseFromList(filmList);
            sendMessage(chatId, count, inlineKeyboardMarkup);
            sendMessage(chatId, CHOOSE_LIST, KeyboardFactory.optionButtons());

            chatStates.put(chatId, AWAITING_OPTION_CHOICE);
        }
    }


    public void deleteChat(long chatId) {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove(true);
        sendMessage(chatId, GOODBYE, replyKeyboardRemove);
        chatStates.remove(chatId);
        userListService.delete(chatId);
    }

    public void replyToUpdate(Update update) {
        Long chatId;

        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getFrom().getId();
            switch (chatStates.get(chatId)) {
                case AWAITING_OPTION_CHOICE -> replyToOptionChoice(chatId, update);
                case AWAITING_FILM_TO_DELETE -> deleteFilm(chatId, update);
            }
        } else {
            Message message = update.getMessage();
            chatId = message.getChatId();
            switch (chatStates.get(chatId)) {
                case AWAITING_FILM_LIST_NAME -> saveFilmList(chatId, message);
                case BACK_TO_START -> mainPage(chatId);
                case AWAITING_OPTION_CHOICE -> replyToOptionChoice(chatId, update);
                case DUMMY -> sendMessage(chatId, "a", KeyboardFactory.logout());
                case AWAITING_FILM_NAME -> saveFilm(chatId, message);
            }
        }
    }

    public void replyToOptionChoice(long chatId, Update update) {


        if (update.hasCallbackQuery()) {
            String listNameCallbackData = update.getCallbackQuery().getData();
            activeFilmList.put(chatId, listNameCallbackData);
            List<Film> filmsList = userListService.findByFilmList(chatId, listNameCallbackData).getFilms();
            if (filmsList.isEmpty()) {
                addFilm(update);
            } else {
                UserList byFilmList = userListService.findByFilmList(chatId, listNameCallbackData);
                int size = byFilmList.getFilms().size();
                String listName = byFilmList.getListName();
                String allFilms = String.format(FILMS, size, listName);
                List<String> films = filmsList.stream()
                        .map(Film::getFilmName)
                        .toList();
                InlineKeyboardMarkup inlineKeyboardMarkup = KeyboardFactory.chooseFromList(films);
                sendMessage(chatId, allFilms, inlineKeyboardMarkup);
                sendMessage(chatId, CHOOSE_FILM, KeyboardFactory.addFilmDeleteFilmBackLogoutButtons());
                chatStates.put(chatId, AWAITING_LIST_SELECTION);
            }

        } else if (update.hasMessage()) {
            String messageText = update.getMessage().getText();
            if (Objects.equals(messageText, CREATE_NEW_LIST)) {
                sendMessage(chatId, CREATE_FILM_LIST);
                chatStates.put(chatId, AWAITING_FILM_LIST_NAME);
            }
        } else {
            // throw
        }
    }

    private void saveFilmList(long chatId, Message message) {
        userListService.save(message);
        sendMessage(chatId, LIST_CREATED);
        mainPage(chatId);
    }

    public void addFilm(Update update){
        Long chatId = update.getCallbackQuery().getFrom().getId();
        sendMessage(chatId, ADD_FILM);
        chatStates.put(chatId, AWAITING_FILM_NAME);
    }

    private void saveFilm(long chatId, Message message) {
        try {
            filmService.save(message, activeFilmList);
            sendMessage(chatId, FILM_ADDED);
        } catch (AlreadyExistsException e) {
            sendMessage(chatId, e.getMessage());
        }
        chatStates.put(chatId, AWAITING_OPTION_CHOICE);
    }

    public void filmToRemoveName(long chatId, String text) {
        sendMessage(chatId, text);
        chatStates.put(chatId, AWAITING_FILM_TO_DELETE);
    }

    private void deleteFilm(long chatId, Update update) {
        String filmName = update.getCallbackQuery().getData();
        try {
            String filmList = activeFilmList.get(chatId);
            filmService.delete(chatId, filmName, filmList);
            String filmRemovedMessage = String.format(FILM_REMOVED, filmName, filmList);
            sendMessage(chatId, filmRemovedMessage);
        } catch (AlreadyExistsException e) {
            sendMessage(chatId, e.getMessage());
        }
        mainPage(chatId);
    }

    private void sendMessage(long chatId, String text, ReplyKeyboard replyKeyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        if (replyKeyboard != null) {
            sendMessage.setReplyMarkup(replyKeyboard);
        }
        sender.execute(sendMessage);
    }

    private void sendMessage(long chatId, String text) {
        sendMessage(chatId, text, null);
    }
}
