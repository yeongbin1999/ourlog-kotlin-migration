package com.back.ourlog.domain.genre.repository

import com.back.ourlog.domain.genre.entity.Genre
import org.springframework.data.jpa.repository.JpaRepository

interface GenreRepository : JpaRepository<Genre, Int> {
    fun findByName(name: String): Genre?
}
