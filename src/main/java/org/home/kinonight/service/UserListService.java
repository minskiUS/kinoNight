package org.home.kinonight.service;


import lombok.AllArgsConstructor;
import org.home.kinonight.exception.AlreadyExistsException;
import org.home.kinonight.exception.NoListNameFoundException;
import org.home.kinonight.model.UserList;
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

    public void save(Message message) {
        String listName = message.getText();
        if (listName.startsWith("/")) {
            // TODO throw exception
        }
        long userId = message.getFrom().getId();
        boolean present = userListRepository.findByUserIDAndListName(userId, listName).isPresent();

        if (present) {
            throw new AlreadyExistsException("List already exists");
        }

        UserList userList = UserList.builder()
                .listName(listName)
                .userId(userId)
                .build();
        userListRepository.save(userList);
    }

    public void delete(long chatId) {
        userListRepository.deleteByUserId(chatId);
    }

    public List<UserList> findByUserId(long chatId) {
        return userListRepository.findByUserId(chatId);
    }

    public UserList findByFilmList(long chatId, String listName) {
        if (listName.startsWith("/")) {
            listName = listName.substring(1);
        }
        Optional<UserList> byUserIDAndListName = userListRepository.findByUserIDAndListName(chatId, listName);
        if (byUserIDAndListName.isPresent()) {
            return byUserIDAndListName.get();
        }
        throw new NoListNameFoundException("List doesn't exist");
    }
}
