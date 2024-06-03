package org.home.kinonight.repository;

import org.home.kinonight.model.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FilmRepository extends JpaRepository<Film, UUID> {

    @Query("select film from Film film where film.filmName = :filmName")
    Optional<Film> findByUserIDAndFilmName(@Param("filmName") String filmName);
}
