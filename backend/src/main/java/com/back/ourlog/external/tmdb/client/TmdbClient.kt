package com.back.ourlog.external.tmdb.client

import com.back.ourlog.external.tmdb.dto.TmdbCreditsResponse
import com.back.ourlog.external.tmdb.dto.TmdbMovieDto
import com.back.ourlog.external.tmdb.dto.TmdbSearchResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriUtils
import java.nio.charset.StandardCharsets

@Component
class TmdbClient(
    private val restTemplate: RestTemplate,
    @Value("\${tmdb.api.key}")
    private val apiKey: String
) {

    private val log = LoggerFactory.getLogger(TmdbClient::class.java)

    fun searchMovie(query: String): TmdbSearchResponse? {
        val encodedQuery = UriUtils.encode(query, StandardCharsets.UTF_8)

        // 한글 + 한국 지역 기준 검색
        val url = "$TMDB_BASE_URL/search/movie?api_key=$apiKey" +
                "&query=$encodedQuery" +
                "&language=ko-KR" +
                "&region=KR" +
                "&include_adult=false"

        var response = restTemplate.getForObject(url, TmdbSearchResponse::class.java)

        // 결과 없을 경우 fallback = 영어로 다시 검색
        if (response?.results.isNullOrEmpty()) {
            val fallbackUrl = "$TMDB_BASE_URL/search/movie?api_key=$apiKey" +
                    "&query=$encodedQuery" +
                    "&language=en-US" +
                    "&region=KR" +
                    "&include_adult=false"

            response = restTemplate.getForObject(fallbackUrl, TmdbSearchResponse::class.java)
        }

        return response
    }

    fun fetchCredits(movieId: Int): TmdbCreditsResponse? {
        val url = "$TMDB_BASE_URL/movie/$movieId/credits?api_key=$apiKey&language=ko-KR"
        return restTemplate.getForObject(url, TmdbCreditsResponse::class.java)
    }

    fun fetchMovieById(id: String): TmdbMovieDto? {
        val url = "$TMDB_BASE_URL/movie/$id?language=ko-KR&api_key=$apiKey"

        return try {
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                TmdbMovieDto::class.java
            )
            response.body
        } catch (e: HttpClientErrorException) {
            throw e
        }
    }

    companion object {
        private const val TMDB_BASE_URL = "https://api.themoviedb.org/3"
    }
}
