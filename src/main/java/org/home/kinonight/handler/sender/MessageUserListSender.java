package org.home.kinonight.handler.sender;

import org.home.kinonight.dto.*;
import org.home.kinonight.factory.KeyboardFactory;
import org.home.kinonight.feign.TelegramClient;
import org.home.kinonight.model.Film;
import org.home.kinonight.model.UserList;
import org.home.kinonight.model.UserState;
import org.home.kinonight.service.UserListService;
import org.home.kinonight.util.TelegramCommandsUtil;
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
    private final Map<Long, String> activeFilmList;
    private final UserListService userListService;
    private final MessageFilmSender messageFilmSender;
    private final TelegramClient telegramClient;

    public MessageUserListSender(SilentSender sender,
                                 Map<Long, UserState> chatStates,
                                 Map<Long, String> activeFilmList,
                                 UserListService userListService,
                                 MessageFilmSender messageFilmSender,
                                 TelegramClient telegramClient) {
        this.sender = sender;
        this.chatStates = chatStates;
        this.activeFilmList = activeFilmList;
        this.userListService = userListService;
        this.messageFilmSender = messageFilmSender;
        this.telegramClient = telegramClient;
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
            SetCommandRequest setCommandRequest = TelegramCommandsUtil.setMyCommands(userLists, chatId);
            telegramClient.setCommand(setCommandRequest);
            GetCommandRequest getCommandRequest = TelegramCommandsUtil.getMyCommands(chatId);
            GetCommandResponse getCommandResponse = telegramClient.getCommand(getCommandRequest);
            List<Command> result = getCommandResponse.getResult();
            List<String> activeCommands = result.stream()
                    .map(Command::getCommand)
                    .toList();

            chatStates.put(chatId, AWAITING_OPTION_CHOICE);
        }
    }

    public void replyToListChoice(long chatId, Update update) {
        String listName;
        if (update.hasCallbackQuery()) {
            listName = update.getCallbackQuery().getData();

        } else {
            listName = update.getMessage().getText();
        }

        activeFilmList.put(chatId, listName);
        List<Film> filmsList = userListService.findByFilmList(chatId, listName).getFilms();
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
}
