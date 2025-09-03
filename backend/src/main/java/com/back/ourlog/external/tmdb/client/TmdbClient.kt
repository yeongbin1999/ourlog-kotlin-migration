package com.back.ourlog.external.tmdb.client

import com.back.ourlog.external.tmdb.dto.TmdbMovieDto
import com.back.ourlog.external.tmdb.dto.TmdbSearchResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.nio.charset.StandardCharsets

@Component
class TmdbClient(
    private val restTemplate: RestTemplate,

    @Value("\${tmdb.api.key}")
    private val apiKey: String
) {

    private val log = LoggerFactory.getLogger(TmdbClient::class.java)

    fun searchMovie(query: String): TmdbSearchResponse? {
        log.info("🔍 TMDB 영화 검색 API 호출: query={}", query)
        val url = buildUrl("/search/movie") {
            queryParam("query", query)
            queryParam("language", "ko-KR")
            queryParam("region", "KR")
            queryParam("include_adult", false)
        }

        var response = restTemplate.getForObject(url, TmdbSearchResponse::class.java)

        if (response?.results.isNullOrEmpty()) {
            log.info("🔍 TMDB 한국어 검색 결과 없음. 영어로 재검색: query={}", query)
            val fallbackUrl = buildUrl("/search/movie") {
                queryParam("query", query)
                queryParam("language", "en-US")
                queryParam("include_adult", false)
            }
            response = restTemplate.getForObject(fallbackUrl, TmdbSearchResponse::class.java)
        }
        return response
    }

    fun fetchMovieById(id: String, appendToResponse: String? = null): TmdbMovieDto? {
        log.info("🎬 TMDB 영화 상세 정보 API 호출: id={}", id)
        val url = buildUrl("/movie/$id") {
            queryParam("language", "ko-KR")
            if (!appendToResponse.isNullOrBlank()) {
                queryParam("append_to_response", appendToResponse)
            }
        }
        return restTemplate.getForObject(url, TmdbMovieDto::class.java)
    }

    private fun buildUrl(path: String, builderAction: UriComponentsBuilder.() -> Unit): String {
        return UriComponentsBuilder.fromHttpUrl(TMDB_BASE_URL)
            .path(path)
            .queryParam("api_key", apiKey)
            .apply(builderAction)
            .encode(StandardCharsets.UTF_8)
            .toUriString()
    }

    companion object {
        private const val TMDB_BASE_URL = "https://api.themoviedb.org/3"
    }
}