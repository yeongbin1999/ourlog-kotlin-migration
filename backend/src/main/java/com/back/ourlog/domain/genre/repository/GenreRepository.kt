package com.back.ourlog.domain.genre.repository;

import com.back.ourlog.domain.genre.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Integer> {
    Optional<Genre> findByName(String name);
}
