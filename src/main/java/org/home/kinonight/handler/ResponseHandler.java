package org.home.kinonight.handler;

import org.home.kinonight.feign.TelegramClient;
import org.home.kinonight.handler.sender.MessageFilmSender;
import org.home.kinonight.handler.sender.MessageUserListSender;
import org.home.kinonight.model.UserState;
import org.home.kinonight.service.FilmService;
import org.home.kinonight.service.UserListService;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.home.kinonight.constants.Messages.CHAT_STATES;
import static org.home.kinonight.constants.Messages.START_TEXT;
import static org.home.kinonight.util.SendMessageUtil.sendMessage;

public class ResponseHandler {
    private final SilentSender sender;
    private final Map<Long, UserState> chatStates;
    private final MessageUserListSender messageUserListSender;
    private final MessageFilmSender messageFilmSender;

    public ResponseHandler(SilentSender sender,
                           DBContext db,
                           UserListService userListService,
                           FilmService filmService,
                           TelegramClient telegramClient) {
        Map<Long, String> activeFilmList = new ConcurrentHashMap<>();
        this.sender = sender;
        chatStates = db.getMap(CHAT_STATES);
        this.messageFilmSender = new MessageFilmSender(sender, chatStates, activeFilmList, userListService, filmService, telegramClient);
        this.messageUserListSender = new MessageUserListSender(sender, chatStates, activeFilmList, userListService, this.messageFilmSender, telegramClient);
    }

    public void replyToStart(Message message) {
        Long chatId = message.getChatId();
        String firstName = message.getFrom().getFirstName();
        String lastName = message.getFrom().getFirstName();
        String welcomeMessage = String.format(START_TEXT, firstName, lastName);
        sendMessage(chatId, welcomeMessage, sender);

        messageUserListSender.mainPage(chatId);
    }

    public void replyToUpdate(Update update) {

        long chatId;

        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getFrom().getId();
            switch (chatStates.get(chatId)) {
                case AWAITING_OPTION_CHOICE -> messageUserListSender.replyToListChoice(chatId, update);
                case AWAITING_FILM_TO_DELETE -> messageFilmSender.removeFilm(chatId, update);
            }
        } else {
            Message message = update.getMessage();
            chatId = message.getChatId();
            switch (chatStates.get(chatId)) {
                case AWAITING_FILM_LIST_NAME -> messageUserListSender.saveFilmList(chatId, message);
                case AWAITING_OPTION_CHOICE -> messageUserListSender.replyToListChoice(chatId, update);
                case AWAITING_FILM_NAME -> messageFilmSender.addFilm(chatId, message);
            }
        }
    }

    public void deleteChat(long chatId) {
        messageUserListSender.deleteChat(chatId);
    }

    public void addFilm(Long chatId) {
        messageFilmSender.filmToAddName(chatId);
    }

    public void removeFilm(long chatId, String text) {
        messageFilmSender.filmToRemoveName(chatId, text);
    }

    public void filmListsMainPage(long chatId) {
        messageUserListSender.mainPage(chatId);
    }

    public void createFilmList(Message message) {
        messageUserListSender.listToAddName(message.getChatId());
    }
}
