package org.home.kinonight.handler;

import org.home.kinonight.constants.Constants;
import org.home.kinonight.model.UserState;
import org.home.kinonight.service.UserListService;

import org.springframework.core.env.Environment;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;

import static org.home.kinonight.constants.Constants.START_TEXT;
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
        chatStates = db.getMap(Constants.CHAT_STATES);
        this.userListService = userListService;
        this.environment = environment;
    }

    public void replyToStart(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(START_TEXT);
        sender.execute(message);
        chatStates.put(chatId, AWAITING_FILM_LIST_NAME);
    }

    public void replyToWelcomeMessage(long chatId, Message message) {
        try {
            this.userListService.save(message);
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("Great! We saved yor list name" + message.getText());
            sender.execute(sendMessage);
            replyToFilmListName(chatId);
            chatStates.put(chatId, AWAITING_OPTION_CHOICE);
        } catch (Exception e) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(e.getMessage());
            sender.execute(sendMessage);
            chatStates.put(chatId, AWAITING_FILM_LIST_NAME);
        }
    }

    private void replyToFilmListName(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Now let's choose between two options");
        sendMessage.setReplyMarkup(addOrRemove());
        sender.execute(sendMessage);
    }

    public ReplyKeyboard addOrRemove() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        boolean present = Arrays.stream(this.environment.getActiveProfiles())
                .anyMatch("dev"::equalsIgnoreCase);
        row.add("Add film");
        row.add("Remove film");
        keyboardRows.add(row);
        if (present) {
            KeyboardRow row1 = new KeyboardRow();
            row1.add(("Delete Chat(WARNING)"));
            keyboardRows.add(row1);
        }
        return new ReplyKeyboardMarkup(keyboardRows);
    }

    private void deleteChat(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Thank you for choosing KinoNight Bot!\nWe hope to see you again!");
        chatStates.remove(chatId);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        sender.execute(sendMessage);
        userListService.delete(chatId);
    }

    public void replyToButtons(long chatId, Message message) {
        if (message.getText().equalsIgnoreCase("Delete Chat(WARNING)")) {
            deleteChat(chatId);
        }

        switch (chatStates.get(chatId)) {
            case AWAITING_FILM_LIST_NAME -> replyToWelcomeMessage(chatId, message);
            case AWAITING_OPTION_CHOICE -> replyToFilmListName(chatId);
        }

    }

    public boolean userIsActive(Long chatId) {
        return chatStates.containsKey(chatId);
    }
}
