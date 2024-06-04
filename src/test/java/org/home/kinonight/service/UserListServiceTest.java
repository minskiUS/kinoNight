package org.home.kinonight.service;

import org.home.kinonight.exception.AlreadyExistsException;
import org.home.kinonight.model.UserList;
import org.home.kinonight.repository.UserListRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

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
    @Captor
    ArgumentCaptor<UserList> userListArgumentCaptor;

    final String actualListName = "list";
    final long actualUserId = 15L;

    @Test
    void save_ShouldSave_WhenCalled() {
        // given
        Message message = generateMessage();
        // when
        testee.save(message);
        // then
        verify(userListRepository).save(userListArgumentCaptor.capture());
        UserList value = userListArgumentCaptor.getValue();
        assertEquals(actualUserId, value.getUserId());
        assertEquals(actualListName, value.getListName());
    }

    @Test
    void save_ShouldThrowException_WhenNameAlreadyExists() {
        // given
        Message message = generateMessage();
        when(userListRepository.findByUserIDAndListName(actualUserId, actualListName)).thenReturn(Optional.of(new UserList()));
        // when
        assertThrows(AlreadyExistsException.class, () -> testee.save(message));
        // then
        verify(userListRepository, never()).save(any());
        verify(userListRepository).findByUserIDAndListName(actualUserId, actualListName);
    }
    @Test
    void delete_ShouldDeleteFromDatabase_WhenCalled(){
        //when
        testee.delete(actualUserId);
        //then
        verify(userListRepository).deleteByUserId(actualUserId);
    }

    private Message generateMessage() {
        Message message = new Message();
        User user = new User();
        user.setId(actualUserId);
        message.setFrom(user);
        message.setText(actualListName);
        return message;
    }
}