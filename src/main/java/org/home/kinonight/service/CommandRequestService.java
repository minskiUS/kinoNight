package org.home.kinonight.service;

import lombok.AllArgsConstructor;
import org.home.kinonight.dto.Command;
import org.home.kinonight.dto.GetCommandRequest;
import org.home.kinonight.dto.GetCommandResponse;
import org.home.kinonight.exception.DoesNotExistException;
import org.home.kinonight.feign.TelegramClient;
import org.home.kinonight.model.ExceptionDetails;
import org.home.kinonight.util.TelegramCommandsUtil;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Service
@AllArgsConstructor
public class CommandRequestService {

    private TelegramClient telegramClient;

    public String checkIfCommandExists(long chatId, Update update) {
        GetCommandRequest getCommandRequest = TelegramCommandsUtil.getMyCommands(chatId);
        GetCommandResponse getCommandResponse = telegramClient.getCommand(getCommandRequest);
        List<Command> result = getCommandResponse.getResult();
        List<String> activeCommands = result.stream()
                .map(Command::getCommand)
                .toList();
        String command;
        if (update.hasCallbackQuery()) {
            command = update.getCallbackQuery().getData();

        } else {
            command = "/" + update.getMessage().getText();
        }
        if (!activeCommands.contains(command)) {
            ExceptionDetails exceptionDetails = new ExceptionDetails(chatId, "Wrong list command");
            throw new DoesNotExistException(exceptionDetails);
        }
        return command;
    }
}
