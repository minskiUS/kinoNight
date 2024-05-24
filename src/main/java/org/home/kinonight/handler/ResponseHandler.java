package org.home.kinonight.handler;

import org.home.kinonight.constants.Constants;
import org.home.kinonight.model.UserState;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;
import java.util.Map;

import static org.home.kinonight.constants.Constants.START_TEXT;
import static org.home.kinonight.model.UserState.*;

public class ResponseHandler {
    private final SilentSender sender;
    private final Map<Long, UserState> chatStates;

    public ResponseHandler(SilentSender sender, DBContext db) {
        this.sender = sender;
        chatStates = db.getMap(Constants.CHAT_STATES);
    }

    public void replyToStart(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(START_TEXT);
        sender.execute(message);
        chatStates.put(chatId, AWAITING_FILM_LIST_NAME);
    }
    private void replyToWelcomeMessage(long chatId, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Great! We saved yor list name" + message.getText());
        sender.execute(sendMessage);
        replyToFilmListName(chatId);
        chatStates.put(chatId, AWAITING_OPTION_CHOICE);
    }

    private void replyToFilmListName(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Now let's choose between two options");
        sendMessage.setReplyMarkup(addOrRemove());
        sender.execute(sendMessage);
    }

    public static ReplyKeyboard addOrRemove() {
        KeyboardRow row = new KeyboardRow();
        row.add("Add film");
        row.add("Remove film");
        return new ReplyKeyboardMarkup(List.of(row));
    }

    public void replyToButtons(long chatId, Message message) {

        switch (chatStates.get(chatId)) {
            case AWAITING_FILM_LIST_NAME -> replyToWelcomeMessage(chatId, message);
            case AWAITING_OPTION_CHOICE -> replyToFilmListName(chatId);
        }

    }
    public boolean userIsActive(Long chatId) {
        return chatStates.containsKey(chatId);
    }
}
