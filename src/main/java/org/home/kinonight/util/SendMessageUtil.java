package org.home.kinonight.util;

import org.home.kinonight.factory.KeyboardFactory;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.List;

public class SendMessageUtil {

    private SendMessageUtil(){}

    public static void sendMessage(long chatId, String text, ReplyKeyboard replyKeyboard, SilentSender sender) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        if (replyKeyboard != null) {
            sendMessage.setReplyMarkup(replyKeyboard);
        }
        sender.execute(sendMessage);
    }

    public static void sendMessage(long chatId, String text, SilentSender sender) {
        sendMessage(chatId, text, null, sender);
    }

    public static void sendMessageWithKeyboard(Long chatId,
                                               int size,
                                               String filmCount,
                                               List<String> names,
                                               SilentSender sender) {
        String count = String.format(filmCount, size);
        InlineKeyboardMarkup inlineKeyboardMarkup = KeyboardFactory.chooseFromList(names);
        sendMessage(chatId, count, inlineKeyboardMarkup, sender);
    }
}
