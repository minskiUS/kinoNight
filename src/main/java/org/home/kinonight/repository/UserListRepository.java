package org.home.kinonight.repository;

import org.home.kinonight.model.UserList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserListRepository extends JpaRepository<UserList, UUID> {

    @Query("delete from UserList userList where userList.userId = :chatId")
    void deleteByUserId(@Param("chatId")long chatId);

    @Query("select userList from UserList userList where userList.userId = :chatId and userList.listName = :listName")
    Optional<UserList> findByUserIDAndListName(@Param("chatId")long chatId, @Param("listName")String listName);
}


