package com.back.ourlog.external.tmdb.service

import com.back.ourlog.domain.content.dto.ContentSearchResultDto
import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.external.tmdb.client.TmdbClient
import com.back.ourlog.external.tmdb.dto.TmdbGenreDto
import com.back.ourlog.external.tmdb.dto.TmdbMovieDto
import com.back.ourlog.global.common.extension.getPosterUrl
import com.back.ourlog.global.common.extension.parseReleaseDateOrNull
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TmdbService(
    private val tmdbClient: TmdbClient
) {

    private val log = LoggerFactory.getLogger(TmdbService::class.java)

    fun searchMovieByExternalId(externalId: String): ContentSearchResultDto {
        return try {
            val id = externalId.removePrefix("tmdb-")
            val movie = tmdbClient.fetchMovieById(id, "credits")
                ?: throw CustomException(ErrorCode.CONTENT_NOT_FOUND)
            toContentSearchResult(movie)
        } catch (e: CustomException) {
            throw e
        } catch (e: Exception) {
            log.error("Failed to search movie by external id: {}. Reason: {}", externalId, e.message, e)
            throw CustomException(ErrorCode.EXTERNAL_API_ERROR)
        }
    }

    fun searchMovieByTitle(title: String): List<ContentSearchResultDto> {
        // 1단계: 제목으로 영화 '검색' (기본 정보만 획득)
        val searchResponse = tmdbClient.searchMovie(title)
        val movieSummaries = searchResponse?.results ?: return emptyList()

        // 2단계: 검색된 영화 ID를 기반으로 '상세 정보'를 다시 조회
        return movieSummaries
            .filterNotNull()
            .filter { !it.posterPath.isNullOrBlank() }
            .sortedByDescending { it.voteCount } // 인기도 순 정렬
            .take(10) // 상위 10개만
            .mapNotNull { summaryDto ->
                // 상세 정보 조회 실패 시 목록에서 제외하기 위해 mapNotNull 사용
                try {
                    // credits(제작진)를 포함하여 상세 정보 조회
                    val detailedMovie = tmdbClient.fetchMovieById(summaryDto.id.toString(), "credits")
                    detailedMovie?.let { toContentSearchResult(it) }
                } catch (e: Exception) {
                    log.error("Failed to fetch movie details for id: {}. Reason: {}", summaryDto.id, e.message)
                    null // 예외 발생 시 null을 반환하여 최종 리스트에서 제외
                }
            }
    }

    private fun toContentSearchResult(movie: TmdbMovieDto): ContentSearchResultDto {
        val directorName = movie.credits?.crew
            ?.filterNotNull()
            ?.firstOrNull { it.job.equals("Director", ignoreCase = true) }
            ?.name

        val genres = extractGenresFromTmdb(movie.genres ?: emptyList())

        return ContentSearchResultDto(
            externalId = "tmdb-${movie.id}",
            title = movie.title ?: "",
            creatorName = directorName ?: "미상",
            description = movie.description ?: "",
            posterUrl = movie.getPosterUrl(),
            releasedAt = movie.parseReleaseDateOrNull(),
            type = ContentType.MOVIE,
            genres = genres
        )
    }

    private fun extractGenresFromTmdb(genreDtos: List<TmdbGenreDto?>): List<String> {
        return genreDtos.mapNotNull { dto -> TMDB_GENRE_MAP[dto?.id] }
    }

    companion object {
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