package org.home.kinonight.responseHandler;

import org.home.kinonight.constants.Constants;
import org.home.kinonight.handler.ResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.HashMap;
import java.util.Map;

import static org.home.kinonight.constants.Constants.START_TEXT;
import static org.home.kinonight.model.UserState.AWAITING_FILM_LIST_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResponseHandlerTest {
    ResponseHandler testee;
    @Mock
    SilentSender sender;
    @Mock
    DBContext db;
    @Captor
    ArgumentCaptor<SendMessage> messageArgumentCaptor;

    private final Map<Object, Object> chatStates = new HashMap<>();
    final long CHAT_ID = 16L;

    @BeforeEach
    public void init(){
        MockitoAnnotations.openMocks(this);
        when(db.getMap(Constants.CHAT_STATES)).thenReturn(chatStates);
        testee = new ResponseHandler(sender, db, null, null);
    }

    @Test
    void replyToStart_ShouldSendWelcomeMessage_AfterStartCommand(){
        // when
        testee.replyToStart(CHAT_ID);
        //then
        assertEquals(AWAITING_FILM_LIST_NAME, chatStates.get(CHAT_ID));
        verify(sender).execute(messageArgumentCaptor.capture());
        SendMessage value = messageArgumentCaptor.getValue();
        assertEquals(String.valueOf(CHAT_ID), value.getChatId());
        assertEquals(START_TEXT, value.getText());
    }
}
