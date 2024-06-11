package org.home.kinonight.util;

import org.home.kinonight.dto.*;
import org.home.kinonight.exception.DoesNotExistException;
import org.home.kinonight.model.ExceptionDetails;
import org.home.kinonight.model.Film;
import org.home.kinonight.model.FilmUserList;
import org.home.kinonight.model.UserList;

import java.util.List;
import java.util.UUID;

import static org.home.kinonight.constants.Messages.*;

public class TelegramCommandsUtil {

    private TelegramCommandsUtil(){}

    public static SetCommandRequest setMyCommands(List<UserList> list, long chatId) {
        Scope scope = createScope(chatId);
        List<CommandRequest> commandRequests = createCommands(list);
        SetCommandRequest setCommandRequest = new SetCommandRequest();
        setCommandRequest.setCommandRequests(commandRequests);
        setCommandRequest.setScope(scope);
        return setCommandRequest;
    }

    public static SetCommandRequest setMyCommandsFilm(List<Film> films, long chatId, UserList userList) {
        Scope scope = createScope(chatId);
        List<CommandRequest> commandRequests = createCommandsFilm(films, userList);
        SetCommandRequest setCommandRequest = new SetCommandRequest();
        setCommandRequest.setCommandRequests(commandRequests);
        setCommandRequest.setScope(scope);
        return setCommandRequest;
    }

    public static DeleteCommandRequest deleteMyCommands(long chatId) {
        return new DeleteCommandRequest(createScope(chatId));
    }

    public static GetCommandRequest getMyCommands(long chatId) {
        return new GetCommandRequest(createScope(chatId));
    }

    private static Scope createScope(long chatId) {
        String type = "chat";
        return new Scope(type, String.valueOf(chatId));
    }

    private static List<CommandRequest> createCommands(List<UserList> userLists) {
        String filmCount = "%d films";
        return userLists.stream()
                .map(userList -> new CommandRequest(userList.getListName(), String.format(filmCount, (userList.getFilmUserLists().size()))))
                .toList();
    }

    private static List<CommandRequest> createCommandsFilm(List<Film> films, UserList userList) {
        return films.stream()
                .map(film -> new CommandRequest(film.getFilmName(), isWatched(film, userList) ? WATCHED : NOT_WATCHED))
                .toList();
    }

    private static boolean isWatched(Film film, UserList userList) {
        UUID userListId = userList.getId();
        return film.getFilmUserLists().stream()
                .filter(filmUserList -> filmUserList.getUserList().getId().equals(userListId))
                .findFirst()
                .map(FilmUserList::isWatched).orElseThrow(() -> new DoesNotExistException(new ExceptionDetails(userList.getUserId(), NOT_FOUND)));
    }
}
