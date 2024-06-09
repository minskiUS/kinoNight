package org.home.kinonight.repository;

import org.home.kinonight.model.FilmUserList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FilmUserListRepository extends JpaRepository<FilmUserList, UUID> {
}
