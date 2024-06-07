package org.home.kinonight.service;

import lombok.AllArgsConstructor;
import org.home.kinonight.exception.AlreadyExistsException;
import org.home.kinonight.exception.DoesNotExistException;
import org.home.kinonight.model.Film;
import org.home.kinonight.model.UserList;
import org.home.kinonight.repository.FilmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.*;
import java.util.function.Predicate;

@Service
@Transactional
@AllArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;
    private final UserListService userListService;

    public void save(Message message, Map<Long, String> activeFilmList) {
        String filmName = message.getText();
        Long chatId = message.getChatId();
        Optional<Film> filmByName = filmRepository.findByFilmName(filmName);
        List<UserList> userLists = new ArrayList<>();
        UUID filmId = null;
        if (filmByName.isPresent()) {
            Film film = filmByName.get();
            filmId = film.getId();
            boolean isFilmInList = film.getUserLists().stream()
                    .anyMatch(getUserListPredicate(chatId, activeFilmList.get(chatId)));
            if (isFilmInList) {
                throw new AlreadyExistsException("Film already in the list");
            } else {
                userLists.addAll(film.getUserLists());
            }
        }
        UserList byFilmList = userListService.findByFilmList(chatId, activeFilmList.get(chatId));
        userLists.add(byFilmList);
        Film film = Film.builder()
                .filmName(filmName)
                .userLists(userLists)
                .build();
        film.setId(filmId);
        filmRepository.save(film);
    }

    public void delete(long chatId, String filmName, String activeFilmList) {

        Optional<Film> filmByName = filmRepository.findByFilmName(filmName);
        if (filmByName.isEmpty()) {
            throw new DoesNotExistException("Film not found");
        }

        Film film = filmByName.get();
        film.getUserLists().removeIf(getUserListPredicate(chatId, activeFilmList));
        filmRepository.save(film);
    }

    private Predicate<UserList> getUserListPredicate(long chatId, String activeFilmList) {
        return userList -> userList.getListName().equalsIgnoreCase(activeFilmList) && chatId == userList.getUserId();
    }
}
