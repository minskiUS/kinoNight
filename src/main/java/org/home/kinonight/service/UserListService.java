package org.home.kinonight.service;


import lombok.AllArgsConstructor;
import org.home.kinonight.exception.ListAlreadyExistsException;
import org.home.kinonight.model.UserList;
import org.home.kinonight.repository.UserListRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;

@AllArgsConstructor
@Transactional
@Service
public class UserListService {
    private final UserListRepository userListRepository;


    public void save(Message message) {
        String listName = message.getText();
        long userId = message.getFrom().getId();
        boolean present = userListRepository.findByUserIDAndListName(userId, listName).isPresent();

        if (present) {
            throw new ListAlreadyExistsException("List already exists");
        }

        UserList userList = UserList.builder()
                .listName(listName)
                .userId(userId)
                .build();
        userListRepository.save(userList);
    }

    @Modifying
    public void delete(long chatId) {
        userListRepository.deleteByUserId(chatId);
    }

}
