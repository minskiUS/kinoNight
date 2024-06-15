package org.home.kinonight.service;

import org.home.kinonight.exception.AlreadyExistsException;
import org.home.kinonight.exception.DoesNotExistException;
import org.home.kinonight.model.ExceptionDetails;
import org.home.kinonight.model.Film;
import org.home.kinonight.model.FilmUserList;
import org.home.kinonight.model.UserList;
import org.home.kinonight.repository.FilmRepository;
import org.home.kinonight.repository.FilmUserListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.*;

import static org.home.kinonight.constants.Messages.FILM_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {
    @InjectMocks
    FilmService testee;
    @Mock
    FilmRepository filmRepository;
    @Mock
    FilmUserListRepository filmUserListRepository;
    @Captor
    ArgumentCaptor<Film> filmArgumentCaptor;
    @Captor
    ArgumentCaptor<FilmUserList> filmUserListArgumentCaptor;

    long actualChatId = 234235L;
    String actualFilmName = "Film";
    String actualListName = "List";
    FilmUserList actualFilmUserList;
    UserList actualUserList;
    Film actualFilm;
    Map<Long, UserList> actualActiveFilmList = new HashMap<>();
    Message actualMessage;

    @BeforeEach
    void setUp() {
        actualUserList = generateUserList();
        actualFilmUserList = generateFilmUserList(false);
        actualFilm = generateFilm();
        actualUserList.setFilmUserLists(List.of(actualFilmUserList));
        actualFilmUserList.setUserList(actualUserList);
        actualFilm.setFilmUserLists(List.of());
        actualFilmUserList.setFilm(actualFilm);
        actualActiveFilmList.put(actualChatId, actualUserList);
        actualMessage = generateMessage();
    }

    @Test
    void save_ShouldSave_WhenCalled() {
        // given
        when(filmRepository.findByFilmName(actualFilmName)).thenReturn(Optional.of(actualFilm));
        Film savedFilm = new Film();
        when(filmRepository.save(filmArgumentCaptor.capture())).thenReturn(savedFilm);
        when(filmUserListRepository.save(filmUserListArgumentCaptor.capture())).thenReturn(null);
        // when
        testee.save(actualMessage, actualActiveFilmList);
        // then
        verify(filmRepository).findByFilmName(actualFilmName);
        Film filmValue = filmArgumentCaptor.getValue();
        assertEquals(actualFilmName, filmValue.getFilmName());
        verify(filmRepository).save(filmValue);
        FilmUserList filmUserListArgumentCaptorValue = filmUserListArgumentCaptor.getValue();
        verify(filmUserListRepository).save(filmUserListArgumentCaptorValue);
        assertEquals(savedFilm, filmUserListArgumentCaptorValue.getFilm());
    }

    @Test
    void save_ShouldThrowException_IfAlreadyExists() {
        // given
        actualFilm.setFilmUserLists(List.of(actualFilmUserList));
        when(filmRepository.findByFilmName(actualFilmName)).thenReturn(Optional.of(actualFilm));
        // when
        AlreadyExistsException alreadyExistsException = assertThrows(AlreadyExistsException.class,
                () -> testee.save(actualMessage, actualActiveFilmList));
        // then
        verify(filmRepository).findByFilmName(actualFilmName);
        verifyNoMoreInteractions(filmRepository);
        verifyNoInteractions(filmUserListRepository);
        ExceptionDetails expectedExceptionDetails = new ExceptionDetails(actualChatId, "Film already in the list");
        assertEquals(expectedExceptionDetails, alreadyExistsException.getExceptionDetails());
    }

    private Message generateMessage() {
        Message message = new Message();
        User user = new User();
        Chat chat = new Chat();
        user.setId(actualChatId);
        message.setFrom(user);
        chat.setId(actualChatId);
        message.setChat(chat);
        message.setText(actualFilmName);
        return message;
    }

    @Test
    void delete_ShouldDelete_WhenCalled() {
        // given
        actualFilm.setFilmUserLists(List.of(actualFilmUserList));
        when(filmRepository.findByFilmName(actualFilmName)).thenReturn(Optional.of(actualFilm));
        // when
        testee.delete(actualFilmName, actualUserList);
        // then
        verify(filmRepository).findByFilmName(actualFilmName);
        verify(filmUserListRepository).delete(actualFilmUserList);
    }

    @Test
    void delete_ShouldThrowException_WhenFilmNotFound() {
        // given
        when(filmRepository.findByFilmName(actualFilmName)).thenReturn(Optional.empty());
        // when
        DoesNotExistException doesNotExistException = assertThrows(DoesNotExistException.class,
                () -> testee.delete(actualFilmName, actualUserList));
        // then
        verify(filmRepository).findByFilmName(actualFilmName);
        verifyNoInteractions(filmUserListRepository);
        ExceptionDetails expectedExceptionDetails = new ExceptionDetails(actualChatId, FILM_NOT_FOUND);
        assertEquals(expectedExceptionDetails, doesNotExistException.getExceptionDetails());
    }

    @Test
    void findByName_ShouldReturnFilm_whenCalled() {
        // given
        when(filmRepository.findByFilmName(actualFilmName)).thenReturn(Optional.of(actualFilm));
        // when
        Film film = testee.findByName(actualFilmName, actualChatId);
        // then
        verify(filmRepository).findByFilmName(actualFilmName);
        assertEquals(actualFilm, film);
    }

    @Test
    void findByName_ShouldThrowException_WhenNotFound() {
        // given
        when(filmRepository.findByFilmName(actualFilmName)).thenReturn(Optional.empty());
        // when
        DoesNotExistException doesNotExistException = assertThrows(DoesNotExistException.class,
                () -> testee.findByName(actualFilmName, actualChatId));
        // then
        verify(filmRepository).findByFilmName(actualFilmName);
        verifyNoMoreInteractions(filmRepository);
        ExceptionDetails expectedExceptionDetails = new ExceptionDetails(actualChatId, FILM_NOT_FOUND);
        assertEquals(expectedExceptionDetails, doesNotExistException.getExceptionDetails());
    }

    @Test
    void markAsWatched_ShouldMarkAsWatched_WhenCalled() {
        // given
        actualFilm.setFilmUserLists(List.of(actualFilmUserList));
        when(filmRepository.findByFilmName(actualFilmName)).thenReturn(Optional.of(actualFilm));
        // when
        testee.markAsWatched(actualFilmName, actualUserList);
        // then
        verify(filmRepository).findByFilmName(actualFilmName);
        verify(filmUserListRepository).save(filmUserListArgumentCaptor.capture());
        assertTrue(filmUserListArgumentCaptor.getValue().isWatched());
    }

    @Test
    void markAsWatched_ShouldThrowException_WhenNotFound() {
        // given
        when(filmRepository.findByFilmName(actualFilmName)).thenReturn(Optional.empty());
        // when
        DoesNotExistException doesNotExistException = assertThrows(DoesNotExistException.class,
                () -> testee.markAsWatched(actualFilmName, actualUserList));
        // then
        verify(filmRepository).findByFilmName(actualFilmName);
        verifyNoInteractions(filmUserListRepository);
        ExceptionDetails expectedExceptionDetails = new ExceptionDetails(actualChatId, FILM_NOT_FOUND);
        assertEquals(expectedExceptionDetails, doesNotExistException.getExceptionDetails());
    }

    @Test
    void markAsWatched_ShouldThrowException_WhenEmpty() {
        // given
        when(filmRepository.findByFilmName(actualFilmName)).thenReturn(Optional.of(actualFilm));
        // when
        DoesNotExistException doesNotExistException = assertThrows(DoesNotExistException.class,
                () -> testee.markAsWatched(actualFilmName, actualUserList));
        // then
        verify(filmRepository).findByFilmName(actualFilmName);
        verifyNoInteractions(filmUserListRepository);
        ExceptionDetails expectedExceptionDetails = new ExceptionDetails(actualChatId, FILM_NOT_FOUND);
        assertEquals(expectedExceptionDetails, doesNotExistException.getExceptionDetails());
    }

    private UserList generateUserList() {
        UserList userList = new UserList();
        userList.setUserId(actualChatId);
        userList.setListName(actualListName);
        userList.setId(UUID.randomUUID());
        return userList;
    }

    private Film generateFilm() {
        return new Film(UUID.randomUUID(), actualFilmName, null);
    }

    private FilmUserList generateFilmUserList(boolean isPresent) {
        if (isPresent) {
            return new FilmUserList(UUID.randomUUID(), null, null, false);
        }
        return new FilmUserList();
    }
}
