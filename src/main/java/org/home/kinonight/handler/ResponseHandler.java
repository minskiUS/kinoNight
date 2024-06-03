package org.home.kinonight.handler;

import org.home.kinonight.factory.KeyboardFactory;
import org.home.kinonight.model.Film;
import org.home.kinonight.model.UserList;
import org.home.kinonight.model.UserState;
import org.home.kinonight.service.FilmService;
import org.home.kinonight.service.UserListService;
import org.springframework.core.env.Environment;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.home.kinonight.constants.Buttons.CREATE_NEW_LIST;
import static org.home.kinonight.constants.Messages.*;
import static org.home.kinonight.model.UserState.*;

public class ResponseHandler {
    private final SilentSender sender;
    private final Map<Long, UserState> chatStates;
    private final UserListService userListService;
    private final FilmService filmService;
    private final Environment environment;

    public ResponseHandler(SilentSender sender,
                           DBContext db,
                           UserListService userListService,
                           Environment environment,
                           FilmService filmService) {
        this.sender = sender;
        chatStates = db.getMap(CHAT_STATES);
        this.userListService = userListService;
        this.environment = environment;
        this.filmService = filmService;
    }

    public void replyToStart(Message message) {
        SendMessage sendWelcomeMessage = new SendMessage();
        Long chatId = message.getChatId();
        sendWelcomeMessage.setChatId(chatId);
        String firstName = message.getFrom().getFirstName();
        String lastName = message.getFrom().getFirstName();
        String welcomeMessage = String.format(START_TEXT, firstName, lastName);
        sendWelcomeMessage.setText(welcomeMessage);
        sender.execute(sendWelcomeMessage);

        mainPage(chatId);
    }

    private void mainPage(Long chatId) {
        List<UserList> byUserId = userListService.findByUserId(chatId);
        if (byUserId.isEmpty()) {
            SendMessage createListMessage = new SendMessage();
            createListMessage.setChatId(chatId);
            createListMessage.setText(CREATE_FILM_LIST);
            sender.execute(createListMessage);
            chatStates.put(chatId, AWAITING_FILM_LIST_NAME);
        } else {
            SendMessage createListMessage = new SendMessage();
            createListMessage.setChatId(chatId);
            int size = byUserId.size();
            String count = String.format(LIST_COUNT, size);
            createListMessage.setText(count);
            List<String> filmList = byUserId.stream()
                    .map(UserList::getListName)
                    .toList();
            InlineKeyboardMarkup inlineKeyboardMarkup = KeyboardFactory.chooseFromList(filmList);
            createListMessage.setReplyMarkup(inlineKeyboardMarkup);
            sender.execute(createListMessage);

            optionButtons(chatId, "Choose", KeyboardFactory.optionButtons());

            chatStates.put(chatId, AWAITING_OPTION_CHOICE);
        }
    }


    public void deleteChat(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Thank you for choosing KinoNight Bot!\nWe hope to see you again!");
        chatStates.remove(chatId);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sender.execute(sendMessage);
        userListService.delete(chatId);
    }

    public void replyToUpdate(Update update) {
        Long chatId;

        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getFrom().getId();
            switch (chatStates.get(chatId)) {
                case AWAITING_OPTION_CHOICE -> replyToOptionChoice(chatId, update);
            }
        } else {
            Message message = update.getMessage();
            chatId = message.getChatId();
            switch (chatStates.get(chatId)) {
                case AWAITING_FILM_LIST_NAME -> saveFilmList(chatId, message);
                case BACK_TO_START -> mainPage(chatId);
                case AWAITING_OPTION_CHOICE -> replyToOptionChoice(chatId, update);
                case DUMMY -> optionButtons(chatId, "a", KeyboardFactory.logout());
                case AWAITING_FILM_NAME -> saveFilm(chatId, message);
            }
        }
    }

    private void replyToOptionChoice(long chatId, Update update) {


        if (update.hasCallbackQuery()) {
            String listNameCallbackData = update.getCallbackQuery().getData();
            List<Film> filmsList = userListService.findByFilmList(chatId, listNameCallbackData).getFilms();
            if (filmsList.isEmpty()) {

                SendMessage createListMessage = new SendMessage();
                createListMessage.setChatId(chatId);
                createListMessage.setText(ADD_FILM);
                sender.execute(createListMessage);
                chatStates.put(chatId, AWAITING_FILM_NAME);
            } else {
                UserList byFilmList = userListService.findByFilmList(chatId, listNameCallbackData);
                SendMessage filmsMessage = new SendMessage();
                filmsMessage.setChatId(chatId);
                int size = byFilmList.getFilms().size();
                String listName = byFilmList.getListName();
                String allFilms = String.format(FILMS, size, listName);
                filmsMessage.setText(allFilms);
                List<String> films = filmsList.stream()
                        .map(Film::getFilmName)
                        .toList();
                InlineKeyboardMarkup inlineKeyboardMarkup = KeyboardFactory.chooseFromList(films);
                filmsMessage.setReplyMarkup(inlineKeyboardMarkup);
                sender.execute(filmsMessage);
            }

        } else if (update.hasMessage()) {
            String messageText = update.getMessage().getText();
            if (Objects.equals(messageText, CREATE_NEW_LIST)) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText(CREATE_FILM_LIST);
                sender.execute(sendMessage);
                chatStates.put(chatId, AWAITING_FILM_LIST_NAME);
            }
        } else {
            // throw
        }
    }

    private void saveFilmList(long chatId, Message message) {
        userListService.save(message);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(LIST_CREATED);
        sender.execute(sendMessage);

        mainPage(chatId);

    }

    private void saveFilm(long chatId, Message message){
        filmService.save(message);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(FILM_ADDED);
        sender.execute(sendMessage);
    }

    private void optionButtons(long chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sender.execute(sendMessage);
    }
}
