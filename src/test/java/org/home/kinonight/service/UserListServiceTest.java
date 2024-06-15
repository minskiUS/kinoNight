package org.home.kinonight.service;

import org.home.kinonight.exception.AlreadyExistsException;
import org.home.kinonight.exception.DoesNotExistException;
import org.home.kinonight.exception.NoListNameFoundException;
import org.home.kinonight.exception.WrongNameException;
import org.home.kinonight.model.ExceptionDetails;
import org.home.kinonight.model.FilmUserList;
import org.home.kinonight.model.UserList;
import org.home.kinonight.repository.FilmUserListRepository;
import org.home.kinonight.repository.UserListRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserListServiceTest {
    @InjectMocks
    UserListService testee;
    @Mock
    UserListRepository userListRepository;
    @Mock
    FilmUserListRepository filmUserListRepository;
    @Captor
    ArgumentCaptor<UserList> userListArgumentCaptor;

    final String actualListName = "list";
    final long actualChatId = 15L;

    @Test
    void save_ShouldSave_WhenCalled() {
        // given
        Message message = generateMessage();
        when(userListRepository.findByUserIDAndListName(actualChatId, actualListName)).thenReturn(Optional.empty());
        // when
        testee.save(message);
        // then
        verify(userListRepository).save(userListArgumentCaptor.capture());
        UserList value = userListArgumentCaptor.getValue();
        assertEquals(actualChatId, value.getUserId());
        assertEquals(actualListName, value.getListName());
    }

    @Test
    void save_ShouldThrowException_WhenNameAlreadyExists() {
        // given
        Message message = generateMessage();
        when(userListRepository.findByUserIDAndListName(actualChatId, actualListName)).thenReturn(Optional.of(new UserList()));
        // when
        AlreadyExistsException alreadyExistsException = assertThrows(AlreadyExistsException.class, () -> testee.save(message));
        // then
        ExceptionDetails expectedExceptionDetails = new ExceptionDetails(actualChatId, "List already exists");
        assertEquals(expectedExceptionDetails, alreadyExistsException.getExceptionDetails());
        verify(userListRepository, never()).save(any());
        verify(userListRepository).findByUserIDAndListName(actualChatId, actualListName);
    }

    @Test
    void save_shouldThrowException_WhenNameStartsWithSlash() {
        // given
        Message message = generateMessage();
        message.setText("/name");
        // when
        WrongNameException wrongNameException = assertThrows(WrongNameException.class, () -> testee.save(message));
        // then
        ExceptionDetails expectedExceptionDetails = new ExceptionDetails(actualChatId, "Name cannot start with '/'\nPlease choose another name");
        assertEquals(expectedExceptionDetails, wrongNameException.getExceptionDetails());
        verifyNoInteractions(userListRepository);
    }
    @Test
    void delete_ShouldDeleteFromDatabase_WhenCalled() {
        // when
        testee.delete(actualChatId);
        // then
        verify(userListRepository).deleteByUserId(actualChatId);
    }

    @Test
    void deleteByListName_ShouldDelete_WhenCalled() {
        // given
        when(userListRepository.findByUserIDAndListName(actualChatId, actualListName)).thenReturn(Optional.of(generateUserList()));
        // when
        testee.deleteByListName(actualChatId, actualListName);
        // then
        verify(filmUserListRepository).deleteAll(List.of(generateFilmUserList()));
        verify(userListRepository).delete(generateUserList());
    }

    @Test
    void deleteByListName_ShouldThrowException_WhenOptionalIsEmpty() {
        // given
        when(userListRepository.findByUserIDAndListName(actualChatId, actualListName)).thenReturn(Optional.empty());
        ExceptionDetails expectedExceptionDetails = new ExceptionDetails(actualChatId, "List not found");
        // when
        DoesNotExistException doesNotExistException = assertThrows(DoesNotExistException.class, () -> testee.deleteByListName(actualChatId, actualListName));
        // then
        verify(userListRepository).findByUserIDAndListName(actualChatId, actualListName);
        assertEquals(expectedExceptionDetails, doesNotExistException.getExceptionDetails());
    }

    @Test
    void findByUserId_ShouldReturnObjectList_WhenCalled(){
        // given
        List<UserList> expectedUserLists = List.of(generateUserList());
        when(userListRepository.findByUserId(actualChatId)).thenReturn(expectedUserLists);
        // when
        List<UserList> actualUserLists = testee.findByUserId(actualChatId);
        // then
        verify(userListRepository).findByUserId(actualChatId);
        assertEquals(expectedUserLists, actualUserLists);
    }

    @Test
    void findByFilmList_shouldReturnObject_WhenCalled() {
        // given
        when(userListRepository.findByUserIDAndListName(actualChatId, actualListName)).thenReturn(Optional.of(generateUserList()));
        // when
        UserList actualFilmList = testee.findByFilmList(actualChatId, "/" + actualListName);
        // then
        verify(userListRepository).findByUserIDAndListName(actualChatId, actualListName);
        assertEquals(generateUserList(), actualFilmList);
    }

    @Test
    void findByFilmList_shouldThrowException_WhenDoesNotExist() {
        // given
        when(userListRepository.findByUserIDAndListName(actualChatId, actualListName)).thenReturn(Optional.empty());
        ExceptionDetails expectedExceptionDetails = new ExceptionDetails(actualChatId, "List doesn't exist");
        // when
        NoListNameFoundException noListNameFoundException = assertThrows(NoListNameFoundException.class, () -> testee.findByFilmList(actualChatId, actualListName));
        // then
        verify(userListRepository).findByUserIDAndListName(actualChatId, actualListName);
        assertEquals(expectedExceptionDetails, noListNameFoundException.getExceptionDetails());
    }

    private Message generateMessage() {
        Message message = new Message();
        User user = new User();
        Chat chat = new Chat();
        user.setId(actualChatId);
        message.setFrom(user);
        chat.setId(actualChatId);
        message.setChat(chat);
        message.setText(actualListName);
        return message;
    }

    private UserList generateUserList() {
        UserList userList = new UserList();
        userList.setUserId(actualChatId);
        userList.setListName(actualListName);
        userList.setFilmUserLists(List.of(generateFilmUserList()));
        return userList;
    }

    private FilmUserList generateFilmUserList() {
        return new FilmUserList();
    }
}