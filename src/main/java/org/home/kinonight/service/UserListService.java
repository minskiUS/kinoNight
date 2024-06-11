package org.home.kinonight.service;


import lombok.AllArgsConstructor;
import org.home.kinonight.exception.AlreadyExistsException;
import org.home.kinonight.exception.DoesNotExistException;
import org.home.kinonight.exception.NoListNameFoundException;
import org.home.kinonight.exception.WrongNameException;
import org.home.kinonight.model.ExceptionDetails;
import org.home.kinonight.model.FilmUserList;
import org.home.kinonight.model.UserList;
import org.home.kinonight.repository.FilmUserListRepository;
import org.home.kinonight.repository.UserListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class UserListService {

    private final UserListRepository userListRepository;
    private final FilmUserListRepository filmUserListRepository;

    public void save(Message message) {

        long chatId = message.getFrom().getId();

        String listName = message.getText();
        if (listName.startsWith("/")) {
            String exceptionMessage = "Name cannot start with '/'\nPlease choose another name";
            ExceptionDetails exceptionDetails = new ExceptionDetails(chatId, exceptionMessage);
            throw new WrongNameException(exceptionDetails);
        }
        boolean present = userListRepository.findByUserIDAndListName(chatId, listName).isPresent();

        if (present) {
            ExceptionDetails exceptionDetails = new ExceptionDetails(message.getChatId(), "List already exists");
            throw new AlreadyExistsException(exceptionDetails);
        }

        UserList userList = UserList.builder()
                .listName(listName)
                .userId(chatId)
                .build();
        userListRepository.save(userList);
    }

    public void delete(long chatId) {
        userListRepository.deleteByUserId(chatId);
    }

    public void deleteByListName(long chatId, String userListName) {
        Optional<UserList> optionalUserList = userListRepository.findByUserIDAndListName(chatId, userListName);
        if (optionalUserList.isEmpty()){
            ExceptionDetails exceptionDetails = new ExceptionDetails(chatId, "List not found");
            throw new DoesNotExistException(exceptionDetails);
        }
        UserList userList = optionalUserList.get();
        List<FilmUserList> filmUserLists = userList.getFilmUserLists();
        filmUserListRepository.deleteAll(filmUserLists);
        userListRepository.delete(userList);
    }

    public List<UserList> findByUserId(long chatId) {
        return userListRepository.findByUserId(chatId);
    }

    public UserList findByFilmList(long chatId, String listName) {
        if (listName.startsWith("/")) {
            listName = listName.substring(1);
        }
        ExceptionDetails exceptionDetails = new ExceptionDetails(chatId, "List doesn't exist");
        return userListRepository.findByUserIDAndListName(chatId, listName)
                .orElseThrow(() -> new NoListNameFoundException(exceptionDetails));
    }
}
