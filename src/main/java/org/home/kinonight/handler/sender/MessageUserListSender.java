package org.home.kinonight.handler.sender;

import org.home.kinonight.dto.*;
import org.home.kinonight.factory.KeyboardFactory;
import org.home.kinonight.feign.TelegramClient;
import org.home.kinonight.model.Film;
import org.home.kinonight.model.FilmUserList;
import org.home.kinonight.model.UserList;
import org.home.kinonight.model.UserState;
import org.home.kinonight.service.CommandRequestService;
import org.home.kinonight.service.UserListService;
import org.home.kinonight.util.TelegramCommandsUtil;
import org.springframework.core.env.Environment;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.List;
import java.util.Map;

import static org.home.kinonight.constants.Messages.*;
import static org.home.kinonight.model.UserState.*;
import static org.home.kinonight.util.SendMessageUtil.sendMessage;
import static org.home.kinonight.util.SendMessageUtil.sendMessageWithKeyboard;

public class MessageUserListSender {

    private final SilentSender sender;
    private final Map<Long, UserState> chatStates;
    private final Map<Long, UserList> activeFilmList;
    private final UserListService userListService;
    private final CommandRequestService commandRequestService;
    private final MessageFilmSender messageFilmSender;
    private final TelegramClient telegramClient;
    private final Environment environment;

    public MessageUserListSender(SilentSender sender,
                                 Map<Long, UserState> chatStates,
                                 Map<Long, UserList> activeFilmList,
                                 UserListService userListService,
                                 CommandRequestService commandRequestService,
                                 MessageFilmSender messageFilmSender,
                                 TelegramClient telegramClient,
                                 Environment environment) {
        this.sender = sender;
        this.chatStates = chatStates;
        this.activeFilmList = activeFilmList;
        this.userListService = userListService;
        this.commandRequestService = commandRequestService;
        this.messageFilmSender = messageFilmSender;
        this.telegramClient = telegramClient;
        this.environment = environment;
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
            sendMessage(chatId, CHOOSE_LIST, KeyboardFactory.optionButtons(environment), sender);
            SetCommandRequest setCommandRequest = TelegramCommandsUtil.setMyCommands(userLists, chatId);
            telegramClient.setCommand(setCommandRequest);
            chatStates.put(chatId, AWAITING_OPTION_CHOICE);
        }
    }

    public void replyToListChoice(long chatId, Update update) {
        String listName = commandRequestService.checkIfCommandExists(chatId, update);
        UserList byListName = userListService.findByFilmList(chatId, listName);
        activeFilmList.put(chatId, byListName);

        List<Film> filmsList = byListName.getFilmUserLists().stream()
                .map(FilmUserList::getFilm)
                .toList();
        if (filmsList.isEmpty()) {
            messageFilmSender.filmToAddName(chatId);
        } else {
            messageFilmSender.mainPage(chatId);
        }
    }

    public void listToAddName(Long chatId) {
        sendMessage(chatId, CREATE_FILM_LIST, sender);
        chatStates.put(chatId, AWAITING_FILM_LIST_NAME);
    }

    public void saveFilmList(long chatId, Message message) {
        userListService.save(message);
        sendMessage(chatId, LIST_CREATED, sender);

        mainPage(chatId);
    }

    public void deleteChat(long chatId) {
        DeleteCommandRequest deleteCommandRequest = TelegramCommandsUtil.deleteMyCommands(chatId);
        telegramClient.deleteCommand(deleteCommandRequest);
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove(true);
        sendMessage(chatId, GOODBYE, replyKeyboardRemove, sender);
        chatStates.remove(chatId);
        userListService.delete(chatId);
    }

    public void listToRemoveName(Long chatId) {
        sendMessage(chatId, REMOVE_LIST_NAME, sender);
        chatStates.put(chatId, AWAITING_LIST_NAME_TO_REMOVE);
    }

    public void removeFilmList(long chatId, Update update){
        String listName = update.getCallbackQuery().getData();
        String listRemovedMessage = String.format(LIST_REMOVED, listName);
        userListService.deleteByListName(chatId, listName);
        sendMessage(chatId, listRemovedMessage, sender);

        mainPage(chatId);
    }
}
