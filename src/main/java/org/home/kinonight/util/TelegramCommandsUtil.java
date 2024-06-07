package org.home.kinonight.util;

import org.home.kinonight.dto.*;
import org.home.kinonight.model.Film;
import org.home.kinonight.model.UserList;

import java.util.List;

public class TelegramCommandsUtil {
    public static SetCommandRequest setMyCommands(List<UserList> list, long chatId){
        Scope scope = createScope(chatId);
        List<Command> commands = createCommands(list);
        SetCommandRequest setCommandRequest = new SetCommandRequest();
        setCommandRequest.setCommands(commands);
        setCommandRequest.setScope(scope);
        return setCommandRequest;
    }

    public static SetCommandRequest setMyCommandsFilm(List<Film> films, long chatId) {
        Scope scope = createScope(chatId);
        List<Command> commands = createCommandsFilm(films);
        SetCommandRequest setCommandRequest = new SetCommandRequest();
        setCommandRequest.setCommands(commands);
        setCommandRequest.setScope(scope);
        return setCommandRequest;
    }

    public static DeleteCommandRequest deleteMyCommands(long chatId){
        return new DeleteCommandRequest(createScope(chatId));
    }

    public static GetCommandRequest getMyCommands(long chatId){
        return new GetCommandRequest(createScope(chatId));
    }

    private static Scope createScope(long chatId){
        String type = "chat";
        return new Scope(type, String.valueOf(chatId));
    }

    private static List<Command> createCommands(List<UserList> userLists){
        String filmCount = "%d films";
        return userLists.stream()
                .map(userList -> new Command(userList.getListName(), String.format(filmCount,(userList.getFilms().size()))))
                .toList();
    }

    private static List<Command> createCommandsFilm(List<Film> films){
        String watched = "watched " + new String(Character.toChars(0x2705));
        String notWatched = "not watched " + new String(Character.toChars(0x274E));
        // TODO if cycle for checkmark
        return films.stream()
                .map(film -> new Command(film.getFilmName(), watched))
                .toList();
    }
}
