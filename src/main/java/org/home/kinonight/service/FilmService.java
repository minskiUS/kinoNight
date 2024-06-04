package org.home.kinonight.service;

import lombok.AllArgsConstructor;
import org.home.kinonight.exception.AlreadyExistsException;
import org.home.kinonight.model.Film;
import org.home.kinonight.model.UserList;
import org.home.kinonight.repository.FilmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        if (filmByName.isPresent()) {
            Film film = filmByName.get();

            boolean isFilmInList = film.getUserLists().stream()
                    .anyMatch(userList -> activeFilmList.get(chatId).equalsIgnoreCase(userList.getListName()) && userList.getUserId() == chatId);
            if (isFilmInList) {
                throw new AlreadyExistsException("Film already in the list");
            } else{
                userLists.addAll(film.getUserLists());
            }
        }
        UserList byFilmList = userListService.findByFilmList(chatId, activeFilmList.get(chatId));
        userLists.add(byFilmList);
        Film film = Film.builder()
                .filmName(filmName)
                .userLists(List.of(byFilmList))
                .build();
        filmRepository.save(film);
    }
}
