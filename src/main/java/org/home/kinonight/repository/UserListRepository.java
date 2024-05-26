package org.home.kinonight.repository;

import org.home.kinonight.model.UserList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserListRepository extends JpaRepository<UserList, UUID> {

}
