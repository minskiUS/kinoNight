package org.home.kinonight.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SendMessageUtilTest {

    @Mock
    SilentSender silentSender;

    long chatId = 15L;
    String text = "Text";
    ReplyKeyboard replyKeyboard = new ReplyKeyboardMarkup();
    @Captor
    ArgumentCaptor<SendMessage> sendMessageArgumentCaptor;

    @Test
    void sendMessage_ShouldSendMessage_WhenCalled() {
        // given
        // when
        SendMessageUtil.sendMessage(chatId, text, replyKeyboard, silentSender);
        // then
        verify(silentSender).execute(sendMessageArgumentCaptor.capture());
        assertEquals(createSendMessage(replyKeyboard), sendMessageArgumentCaptor.getValue());
    }

    @Test
    void sendMessage_ShouldSendMessage_WithNoReplyMarkup() {
        // when
        SendMessageUtil.sendMessage(chatId, text, silentSender);
        // then
        verify(silentSender).execute(sendMessageArgumentCaptor.capture());
        assertEquals(createSendMessage(null), sendMessageArgumentCaptor.getValue());
    }

    @Test
    void sendMessageWithKeyboard_ShouldSendMessageWithKeyboardWhenCalled() {
        // given
        int size = 3;
        String filmCount = "%s";
        String name = "name";
        // when
        SendMessageUtil.sendMessageWithKeyboard(chatId, size, filmCount, List.of(name), silentSender);
        // then
        verify(silentSender).execute(sendMessageArgumentCaptor.capture());
        SendMessage value = sendMessageArgumentCaptor.getValue();
        assertEquals("3", value.getText());
        InlineKeyboardMarkup replyMarkup = (InlineKeyboardMarkup) (value.getReplyMarkup());
        assertEquals(1, replyMarkup.getKeyboard().size());
        assertEquals(1, replyMarkup.getKeyboard().getFirst().size());
    }

    private SendMessage createSendMessage(ReplyKeyboard replyKeyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(replyKeyboard);
        return sendMessage;
    }
}