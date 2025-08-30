package com.back.ourlog.external.tmdb.service

import com.back.ourlog.domain.content.dto.ContentSearchResultDto
import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.external.tmdb.client.TmdbClient
import com.back.ourlog.external.tmdb.dto.TmdbGenreDto
import com.back.ourlog.external.tmdb.dto.TmdbMovieDto
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class TmdbService(
    private val tmdbClient: TmdbClient
) {

    private val log = LoggerFactory.getLogger(TmdbService::class.java)

    fun searchMovieByExternalId(externalId: String): ContentSearchResultDto {
        return try {
            val id = externalId.removePrefix("tmdb-")
            val movie = tmdbClient.fetchMovieById(id)
                ?: throw CustomException(ErrorCode.CONTENT_NOT_FOUND)
            toContentSearchResult(movie)
        } catch (e: Exception) {
            throw CustomException(ErrorCode.CONTENT_NOT_FOUND)
        }
    }

    fun searchMovieByTitle(title: String): List<ContentSearchResultDto> {
        val response = tmdbClient.searchMovie(title)
        val movies = response?.results ?: emptyList()

        return movies
            .filterNotNull()
            .filter { movie ->
                movie.title?.lowercase()?.contains(title.lowercase()) == true
            }
            .sortedByDescending { it.voteCount ?: 0 }
            .take(10)
            .mapNotNull { movie ->
                val id = "tmdb-${movie.id}"
                try {
                    searchMovieByExternalId(id)
                } catch (e: CustomException) {
                    null
                }
            }
    }

    private fun toContentSearchResult(movie: TmdbMovieDto): ContentSearchResultDto {
        val directorName = fetchDirectorName(movie.id)
        val genres = extractGenresFromTmdb(movie.genres ?: emptyList())

        return ContentSearchResultDto(
            externalId = "tmdb-${movie.id}",
            title = movie.title,
            creatorName = directorName,
            description = movie.description,
            posterUrl = POSTER_BASE_URL + movie.posterPath,
            releasedAt = parseDate(movie.releaseDate),
            type = ContentType.MOVIE,
            genres = genres
        )
    }

    private fun fetchDirectorName(movieId: Int): String? {
        return try {
            val credits = tmdbClient.fetchCredits(movieId)
            credits?.crew
                ?.filterNotNull()
                ?.firstOrNull { it.job.equals("Director", ignoreCase = true) }
                ?.name
        } catch (e: Exception) {
            null
        }
    }

    private fun parseDate(date: String?): LocalDateTime? {
        return try {
            date?.let { LocalDate.parse(it).atStartOfDay() }
        } catch (e: Exception) {
            null
        }
    }

    private fun extractGenresFromTmdb(genreDtos: List<TmdbGenreDto?>): List<String> {
        return genreDtos
            .mapNotNull { dto -> TMDB_GENRE_MAP[dto?.id] }
    }

    companion object {
        private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"

        private val TMDB_GENRE_MAP: Map<Int, String> = mapOf(
            28 to "액션",
            12 to "모험",
            16 to "애니메이션",
            35 to "코미디",
            80 to "범죄",
            99 to "다큐멘터리",
            18 to "드라마",
            10751 to "가족",
            14 to "판타지",
            36 to "역사",
            27 to "공포",
            10402 to "음악",
            9648 to "미스터리",
            10749 to "로맨스",
            878 to "SF",
            10770 to "TV 영화",
            53 to "스릴러",
            10752 to "전쟁",
            37 to "서부"
        )
    }
}
