package org.home.kinonight.service;


import lombok.AllArgsConstructor;
import org.home.kinonight.model.UserList;
import org.home.kinonight.repository.UserListRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@AllArgsConstructor
@Service
public class UserListService {
    private final UserListRepository userListRepository;


    public void save(Message message) {
        UserList userList = UserList.builder().listName(message.getText()).userId(message.getFrom().getId()).build();
        userListRepository.save(userList);
    }

}
