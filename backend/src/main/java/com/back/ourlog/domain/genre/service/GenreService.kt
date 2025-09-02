package com.back.ourlog.domain.genre.service

import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.domain.diary.entity.Diary
import com.back.ourlog.domain.genre.entity.DiaryGenre
import com.back.ourlog.domain.genre.entity.Genre
import com.back.ourlog.domain.genre.repository.GenreRepository
import com.back.ourlog.external.library.service.LibraryService
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GenreService(
    private val genreRepository: GenreRepository,
    private val libraryService: LibraryService
) {
    fun getGenresByIds(ids: List<Int>): List<Genre> {
        val genres = genreRepository.findAllById(ids)
        if (genres.isEmpty()) throw CustomException(ErrorCode.GENRE_NOT_FOUND)
        return genres
    }

    fun findOrCreateByName(name: String): Genre =
        genreRepository.findByName(name) ?: genreRepository.save(Genre(name))

    // BOOK이면 KDC → 장르명 매핑, 공백/중복 정리
    fun normalizeGenreNames(rawNames: List<String>?, type: ContentType): List<String> =
        (rawNames ?: emptyList())
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { n -> if (type == ContentType.BOOK) libraryService.mapKdcToGenre(n) else n }
            .distinct()

    // 다이어리-장르 동기화
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    fun syncDiaryGenres(diary: Diary, rawNames: List<String>?, type: ContentType) {
        val mapped  = normalizeGenreNames(rawNames, type)
        val current = diary.diaryGenres.map { it.genre.name }.toSet()

        val toAdd    = mapped.filter { it !in current }
        val toRemove = current.filter { it !in mapped }

        if (toAdd.isEmpty() && toRemove.isEmpty()) return // 변경 없음

        // 제거
        val it = diary.diaryGenres.iterator()
        while (it.hasNext()) {
            val rel = it.next()
            if (rel.genre.name in toRemove) it.remove()
        }

        // 추가
        toAdd.forEach { name ->
            diary.diaryGenres.add(DiaryGenre(diary, findOrCreateByNameSafe(name))) // ← 안전 버전 사용 (아래 추가)
        }
    }

    private fun findOrCreateByNameSafe(name: String): Genre {
        genreRepository.findByName(name)?.let { return it }
        return try {
            genreRepository.save(Genre(name))
        } catch (e: DataIntegrityViolationException) {
            genreRepository.findByName(name) ?: throw e
        }
    }

}
