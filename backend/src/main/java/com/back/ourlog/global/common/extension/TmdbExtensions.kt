package com.back.ourlog.global.common.extension

import com.back.ourlog.external.tmdb.dto.TmdbMovieDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"

// TMDB의 포스터 경로를 완전한 이미지 URL로 변환
fun TmdbMovieDto.getPosterUrl(): String? {
    return posterPath?.takeIf { it.isNotBlank() }?.let { "$POSTER_BASE_URL$it" }
}

// TMDB의 releaseDate (yyyy-MM-dd) 문자열을 LocalDateTime으로 변환
fun TmdbMovieDto.parseReleaseDateOrNull(): LocalDateTime? {
    return try {
        releaseDate?.let { LocalDate.parse(it).atStartOfDay() }
    } catch (e: DateTimeParseException) {
        null
    }
}
