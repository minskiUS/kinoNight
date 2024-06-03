package org.home.kinonight.handler;

import org.home.kinonight.constants.Messages;
import org.home.kinonight.factory.KeyboardFactory;
import org.home.kinonight.model.UserList;
import org.home.kinonight.model.UserState;
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
import static org.home.kinonight.model.UserState.AWAITING_FILM_LIST_NAME;
import static org.home.kinonight.model.UserState.AWAITING_OPTION_CHOICE;

public class ResponseHandler {
    private final SilentSender sender;
    private final Map<Long, UserState> chatStates;
    private final UserListService userListService;
    private final Environment environment;

    public ResponseHandler(SilentSender sender,
                           DBContext db,
                           UserListService userListService,
                           Environment environment) {
        this.sender = sender;
        chatStates = db.getMap(Messages.CHAT_STATES);
        this.userListService = userListService;
        this.environment = environment;
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
            InlineKeyboardMarkup inlineKeyboardMarkup = KeyboardFactory.chooseFilmList(filmList);
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
        Message message = update.getMessage();
        Long chatId = message.getChatId();

        switch (chatStates.get(chatId)) {
            case AWAITING_FILM_LIST_NAME -> saveFilmList(chatId, message);
            case BACK_TO_START -> mainPage(chatId);
            case AWAITING_OPTION_CHOICE -> replyToOptionChoice(chatId, update);
            case DUMMY -> optionButtons(chatId, "a", KeyboardFactory.logout());
        }

    }

    private void replyToOptionChoice(long chatId, Update update) {
        String messageText = update.getMessage().getText();
        String listNameCallbackData = update.getCallbackQuery().getData();
        if (Objects.equals(messageText, CREATE_NEW_LIST)) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(CREATE_FILM_LIST);
            sender.execute(sendMessage);
            chatStates.put(chatId, AWAITING_FILM_LIST_NAME);

        } else {
            UserList byFilmList = userListService.findByFilmList(chatId, listNameCallbackData);
            System.out.println(byFilmList.getListName());
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

    /*public boolean userIsActive(Long chatId) {
        return chatStates.containsKey(chatId);
    }*/

    private void optionButtons(long chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sender.execute(sendMessage);
    }
}
