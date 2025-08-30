package com.back.ourlog.domain.genre.service

import com.back.ourlog.domain.genre.entity.Genre
import com.back.ourlog.domain.genre.repository.GenreRepository
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.springframework.stereotype.Service

@Service
class GenreService(
    private val genreRepository: GenreRepository
) {

    fun getGenresByIds(ids: List<Int>): List<Genre> {
        val genres = genreRepository.findAllById(ids)
        if (genres.isEmpty()) {
            throw CustomException(ErrorCode.GENRE_NOT_FOUND)
        }
        return genres
    }

    fun findOrCreateByName(name: String): Genre {
        return genreRepository.findByName(name)
            ?: genreRepository.save(Genre(name))
    }
}
