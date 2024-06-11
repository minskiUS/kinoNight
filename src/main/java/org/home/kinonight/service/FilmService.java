package org.home.kinonight.service;

import lombok.AllArgsConstructor;
import org.home.kinonight.exception.AlreadyExistsException;
import org.home.kinonight.exception.DoesNotExistException;
import org.home.kinonight.model.ExceptionDetails;
import org.home.kinonight.model.Film;
import org.home.kinonight.model.FilmUserList;
import org.home.kinonight.model.UserList;
import org.home.kinonight.repository.FilmRepository;
import org.home.kinonight.repository.FilmUserListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import static org.home.kinonight.constants.Messages.FILM_NOT_FOUND;

@Service
@Transactional
@AllArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;
    private final FilmUserListRepository filmUserListRepository;

    public void save(Message message, Map<Long, UserList> activeFilmList) {
        String filmName = message.getText();
        Long chatId = message.getChatId();
        Optional<Film> filmByName = filmRepository.findByFilmName(filmName);
        UUID filmId = UUID.randomUUID();
        UserList activeUserList = activeFilmList.get(chatId);
        if (filmByName.isPresent()) {
            Film film = filmByName.get();
            filmId = film.getId();
            boolean isFilmInList = film.getFilmUserLists().stream()
                    .anyMatch(getFilmUserListsPredicate(activeUserList));
            if (isFilmInList) {
                ExceptionDetails exceptionDetails = new ExceptionDetails(chatId, "Film already in the list");
                throw new AlreadyExistsException(exceptionDetails);
            }
        }

        Film film = Film.builder()
                .id(filmId)
                .filmName(filmName)
                .build();
        Film savedFilm = filmRepository.save(film);

        FilmUserList newFilmUserList = FilmUserList.builder()
                .userList(activeUserList)
                .film(savedFilm)
                .build();
        filmUserListRepository.save(newFilmUserList);
    }

    public void delete(String filmName, UserList activeFilmList) {

        Optional<Film> filmByName = filmRepository.findByFilmName(filmName);
        if (filmByName.isEmpty()) {
            ExceptionDetails exceptionDetails = new ExceptionDetails(activeFilmList.getUserId(), FILM_NOT_FOUND);
            throw new DoesNotExistException(exceptionDetails);
        }

        Film film = filmByName.get();
        film.getFilmUserLists()
                .stream()
                .filter(getFilmUserListsPredicate(activeFilmList))
                .findFirst()
                .ifPresent(filmUserListRepository::delete);
    }

    private Predicate<FilmUserList> getFilmUserListsPredicate(UserList activeFilmList) {
        return filmUserList -> filmUserList.getUserList().equals(activeFilmList);
    }

    public Film findByName(String filmName, long chatId) {
        if (filmRepository.findByFilmName(filmName).isEmpty()) {
            ExceptionDetails exceptionDetails = new ExceptionDetails(chatId, FILM_NOT_FOUND);
            throw new DoesNotExistException(exceptionDetails);
        }
        return filmRepository.findByFilmName(filmName).get();
        }
        // TODO improve repository with UserList

    public void markAsWatched(String filmName, UserList userList) {

        Optional<Film> optionalFilm = filmRepository.findByFilmName(filmName);
        ExceptionDetails exceptionDetails = new ExceptionDetails(userList.getUserId(), FILM_NOT_FOUND);
        if (optionalFilm.isEmpty()){
            throw new DoesNotExistException(exceptionDetails);
        }

        Film film = optionalFilm.get();
        Optional<FilmUserList> optionalFilmUserList = film.getFilmUserLists().stream()
                .filter(filmUserList -> filmUserList.getUserList().getId().equals(userList.getId()))
                .findFirst();
        if (optionalFilmUserList.isEmpty()){
            throw new DoesNotExistException(exceptionDetails);
        }
        FilmUserList selectedFilmUserList = optionalFilmUserList.get();
        selectedFilmUserList.setWatched(!selectedFilmUserList.isWatched());
        filmUserListRepository.save(selectedFilmUserList);
    }
}
