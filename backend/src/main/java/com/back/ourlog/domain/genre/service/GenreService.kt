package com.back.ourlog.domain.genre.service

import com.back.ourlog.domain.genre.entity.Genre
import com.back.ourlog.domain.genre.repository.GenreRepository
import org.springframework.stereotype.Service

@Service
class GenreService(
    private val genreRepository: GenreRepository
) {

    fun getGenresByIds(ids: List<Int>): List<Genre> {
        return genreRepository.findAllById(ids)
    }

    fun findOrCreateByName(name: String): Genre {
        return genreRepository.findByName(name)
            ?: genreRepository.save(Genre(name))
    }
}
