package org.home.kinonight.util;

import org.home.kinonight.dto.*;
import org.home.kinonight.model.Film;
import org.home.kinonight.model.FilmUserList;
import org.home.kinonight.model.UserList;

import java.util.List;
import java.util.UUID;

import static org.home.kinonight.constants.Messages.NOT_WATCHED;
import static org.home.kinonight.constants.Messages.WATCHED;

public class TelegramCommandsUtil {
    public static SetCommandRequest setMyCommands(List<UserList> list, long chatId) {
        Scope scope = createScope(chatId);
        List<Command> commands = createCommands(list);
        SetCommandRequest setCommandRequest = new SetCommandRequest();
        setCommandRequest.setCommands(commands);
        setCommandRequest.setScope(scope);
        return setCommandRequest;
    }

    public static SetCommandRequest setMyCommandsFilm(List<Film> films, long chatId, UserList userList) {
        Scope scope = createScope(chatId);
        List<Command> commands = createCommandsFilm(films, userList);
        SetCommandRequest setCommandRequest = new SetCommandRequest();
        setCommandRequest.setCommands(commands);
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

    private static List<Command> createCommands(List<UserList> userLists) {
        String filmCount = "%d films";
        return userLists.stream()
                .map(userList -> new Command(userList.getListName(), String.format(filmCount, (userList.getFilmUserLists().size()))))
                .toList();
    }

    private static List<Command> createCommandsFilm(List<Film> films, UserList userList) {
        return films.stream()
                .map(film -> new Command(film.getFilmName(), isWatched(film, userList) ? WATCHED : NOT_WATCHED))
                .toList();
    }

    private static boolean isWatched(Film film, UserList userList) {
        UUID userListId = userList.getId();
        return film.getFilmUserLists().stream()
                .filter(filmUserList -> filmUserList.getUserList().getId().equals(userListId))
                .findFirst()
                .map(FilmUserList::isWatched).get();
    }
}
