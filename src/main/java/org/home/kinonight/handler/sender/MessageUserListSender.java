package org.home.kinonight.handler.sender;

import org.home.kinonight.factory.KeyboardFactory;
import org.home.kinonight.model.Film;
import org.home.kinonight.model.UserList;
import org.home.kinonight.model.UserState;
import org.home.kinonight.service.UserListService;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.home.kinonight.constants.Buttons.CREATE_NEW_LIST;
import static org.home.kinonight.constants.Messages.*;
import static org.home.kinonight.factory.KeyboardFactory.addFilmDeleteFilmBackLogoutButtons;
import static org.home.kinonight.model.UserState.*;
import static org.home.kinonight.util.SendMessageUtil.sendMessage;
import static org.home.kinonight.util.SendMessageUtil.sendMessageWithKeyboard;

public class MessageUserListSender {

    private final SilentSender sender;
    private final Map<Long, UserState> chatStates;
    private final Map<Long, String> activeFilmList;
    private final UserListService userListService;
    private final MessageFilmSender messageFilmSender;

    public MessageUserListSender(SilentSender sender,
                                 Map<Long, UserState> chatStates,
                                 Map<Long, String> activeFilmList,
                                 UserListService userListService,
                                 MessageFilmSender messageFilmSender) {
        this.sender = sender;
        this.chatStates = chatStates;
        this.activeFilmList = activeFilmList;
        this.userListService = userListService;
        this.messageFilmSender = messageFilmSender;
    }

    public void mainPage(Long chatId) {
        List<UserList> byUserId = userListService.findByUserId(chatId);
        if (byUserId.isEmpty()) {
            sendMessage(chatId, CREATE_FILM_LIST, sender);
            chatStates.put(chatId, AWAITING_FILM_LIST_NAME);
        } else {
            List<UserList> userLists = userListService.findByUserId(chatId);
            List<String> listNames = userLists.stream().map(UserList::getListName).toList();
            sendMessageWithKeyboard(chatId, byUserId.size(), LIST_COUNT, listNames, sender);
            sendMessage(chatId, CHOOSE_LIST, KeyboardFactory.optionButtons(), sender);

            chatStates.put(chatId, AWAITING_OPTION_CHOICE);
        }
    }

    public void replyToOptionChoice(long chatId, Update update) {
        if (update.hasCallbackQuery()) {
            String listNameCallbackData = update.getCallbackQuery().getData();
            activeFilmList.put(chatId, listNameCallbackData);
            List<Film> filmsList = userListService.findByFilmList(chatId, listNameCallbackData).getFilms();
            if (filmsList.isEmpty()) {
                messageFilmSender.filmToAddName(chatId);
            } else {
                UserList byFilmList = userListService.findByFilmList(chatId, listNameCallbackData);
                int size = byFilmList.getFilms().size();
                String listName = byFilmList.getListName();
                String allFilms = String.format(FILMS, size, listName);
                List<String> films = filmsList.stream()
                        .map(Film::getFilmName)
                        .toList();
                InlineKeyboardMarkup inlineKeyboardMarkup = KeyboardFactory.chooseFromList(films);
                sendMessage(chatId, allFilms, inlineKeyboardMarkup, sender);
                sendMessage(chatId, CHOOSE_FILM, addFilmDeleteFilmBackLogoutButtons(), sender);
                chatStates.put(chatId, AWAITING_LIST_SELECTION);
            }

        } else if (update.hasMessage()) {
            String messageText = update.getMessage().getText();
            if (Objects.equals(messageText, CREATE_NEW_LIST)) {
                sendMessage(chatId, CREATE_FILM_LIST, sender);
                chatStates.put(chatId, AWAITING_FILM_LIST_NAME);
            }
        } else {
            // throw
        }
    }

    public void saveFilmList(long chatId, Message message) {
        userListService.save(message);
        sendMessage(chatId, LIST_CREATED, sender);
        mainPage(chatId);
    }

    public void deleteChat(long chatId) {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove(true);
        sendMessage(chatId, GOODBYE, replyKeyboardRemove, sender);
        chatStates.remove(chatId);
        userListService.delete(chatId);
    }
}
