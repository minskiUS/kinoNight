package org.home.kinonight.responseHandler;

import org.home.kinonight.constants.Constants;
import org.home.kinonight.exception.ListAlreadyExistsException;
import org.home.kinonight.handler.ResponseHandler;
import org.home.kinonight.service.UserListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.home.kinonight.constants.Constants.START_TEXT;
import static org.home.kinonight.model.UserState.AWAITING_FILM_LIST_NAME;
import static org.home.kinonight.model.UserState.AWAITING_OPTION_CHOICE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResponseHandlerTest {
    ResponseHandler testee;
    @Mock
    SilentSender sender;
    @Mock
    UserListService userListService;
    @Mock
    DBContext db;
    @Mock
    Environment environment;
    @Captor
    ArgumentCaptor<SendMessage> messageArgumentCaptor;

    final String actualListName = "list";
    private final Map<Object, Object> chatStates = new HashMap<>();
    final long CHAT_ID = 16L;

    @BeforeEach
    public void init(){
        MockitoAnnotations.openMocks(this);
        when(db.getMap(Constants.CHAT_STATES)).thenReturn(chatStates);
        testee = new ResponseHandler(sender, db, userListService, environment);
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

    @Test
    void replyToWelcomeMessage_ShouldProvideOptionButtons_AfterNameTyped(){
        // given
        Message message = generateMessage();
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        // when
        testee.replyToWelcomeMessage(CHAT_ID, message);
        // then
        verify(userListService).save(message);
        verify(sender, times(2)).execute(messageArgumentCaptor.capture());
        List<SendMessage> value = messageArgumentCaptor.getAllValues();
        assertEquals(String.valueOf(CHAT_ID), value.getFirst().getChatId());
        assertEquals("Great! We saved yor list name" + message.getText(), value.getFirst().getText());
        assertEquals(String.valueOf(CHAT_ID), value.get(1).getChatId());
        assertEquals("Now let's choose between two options", value.get(1).getText());
        assertEquals(AWAITING_OPTION_CHOICE, chatStates.get(CHAT_ID));
    }

    @Test
    void replyToWelcomeMessage_ShouldCatchExceptionAndSendExceptionMessage_AfterNameTyped(){
        // given
        Message message = generateMessage();
        doThrow(new ListAlreadyExistsException("msg")).when(userListService).save(message);
        // when
        testee.replyToWelcomeMessage(CHAT_ID, message);
        // then
        verify(userListService).save(message);
        verify(sender).execute(messageArgumentCaptor.capture());
        SendMessage value = messageArgumentCaptor.getValue();
        assertEquals(String.valueOf(CHAT_ID), value.getChatId());
        assertEquals("msg", value.getText());
        assertEquals(AWAITING_FILM_LIST_NAME, chatStates.get(CHAT_ID));
    }

    private Message generateMessage() {
        Message message = new Message();
        User user = new User();
        user.setId(CHAT_ID);
        message.setFrom(user);
        message.setText(actualListName);
        return message;
    }
}
