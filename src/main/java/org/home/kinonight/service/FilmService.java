package org.home.kinonight.service;

import lombok.AllArgsConstructor;
import org.home.kinonight.exception.ListAlreadyExistsException;
import org.home.kinonight.model.Film;
import org.home.kinonight.repository.FilmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@Transactional
@AllArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;

    public void save(Message message) {
        String filmName = message.getText();
        boolean present = filmRepository.findByUserIDAndFilmName(filmName).isPresent();

        if (present) {
            throw new ListAlreadyExistsException("Film already in the list");
        }

        Film film = Film.builder()
                .filmName(filmName)
                .build();
        filmRepository.save(film);
    }
}
