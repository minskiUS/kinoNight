package org.home.kinonight.service;

import org.home.kinonight.dto.CommandRequest;
import org.home.kinonight.dto.GetCommandRequest;
import org.home.kinonight.dto.GetCommandResponse;
import org.home.kinonight.dto.Scope;
import org.home.kinonight.exception.DoesNotExistException;
import org.home.kinonight.feign.TelegramClient;
import org.home.kinonight.model.ExceptionDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandRequestServiceTest {
    @InjectMocks
    CommandRequestService testee;
    @Mock
    TelegramClient actualTelegramClient;
    long chatId = 76445L;
    static String command = "thisCommand";
    @Captor
    ArgumentCaptor<GetCommandRequest> getCommandRequestArgumentCaptor;


    @ParameterizedTest
    @MethodSource("generateParameters")
    void checkIfCommandExists_ShouldReturnString_WhenCalled(Update update, String expectedCommand) {
        // given
        when(actualTelegramClient.getCommand(getCommandRequestArgumentCaptor.capture()))
                .thenReturn(generateCommandResponse());
        // when
        String actualCommand = testee.checkIfCommandExists(chatId, update);
        // then
        assertEquals(expectedCommand, actualCommand);
        GetCommandRequest getCommandRequestArgumentCaptorValue = getCommandRequestArgumentCaptor.getValue();
        Scope scope = getCommandRequestArgumentCaptorValue.getScope();
        assertNotNull(scope);
        assertEquals(String.valueOf(chatId), scope.getChatId());
    }

    @Test
    void checkIfCommandExists_ShouldThrowException_WhenWrongCommand() {
        // given
        GetCommandResponse getCommandResponse = generateCommandResponse();
        getCommandResponse.setResult(List.of());
        when(actualTelegramClient.getCommand(getCommandRequestArgumentCaptor.capture()))
                .thenReturn(getCommandResponse);
        // when
        DoesNotExistException doesNotExistException = assertThrows(DoesNotExistException.class, () -> testee.checkIfCommandExists(chatId, generateUpdate(true, false)));
        // then
        ExceptionDetails expectedExceptionDetails = new ExceptionDetails(chatId, "Wrong list command");
        assertEquals(expectedExceptionDetails, doesNotExistException.getExceptionDetails());
    }

    private static Update generateUpdate(boolean hasCallbackQuery, boolean isMessageHasSlash) {
        Update update = new Update();
        if (hasCallbackQuery) {
            CallbackQuery callbackQuery = new CallbackQuery();
            callbackQuery.setData(command);
            update.setCallbackQuery(callbackQuery);
        } else {
            Message message = new Message();
            message.setText(isMessageHasSlash ? "/" + command : command);
            update.setMessage(message);
        }
        return update;
    }

    private GetCommandResponse generateCommandResponse() {
        GetCommandResponse getCommandResponse = new GetCommandResponse();
        CommandRequest commandRequest = new CommandRequest(command, "");
        getCommandResponse.setResult(List.of(commandRequest));
        return getCommandResponse;
    }

    public static Stream<Arguments> generateParameters() {
        return Stream.of(
                Arguments.of(generateUpdate(true, false),command),
                Arguments.of(generateUpdate(false, false),command),
                Arguments.of(generateUpdate(false, true),command)
        );
    }
}
