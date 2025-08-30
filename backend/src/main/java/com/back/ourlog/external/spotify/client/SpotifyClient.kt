package com.back.ourlog.external.spotify.client

import com.back.ourlog.external.spotify.dto.SpotifySearchResponse
import com.back.ourlog.external.spotify.dto.SpotifyTokenResponse
import com.back.ourlog.external.spotify.dto.TrackItem
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException.Unauthorized
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriUtils
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.*

@Component
class SpotifyClient(
    private val restTemplate: RestTemplate,
    @Value("\${spotify.client-id}")
    private val clientId: String,

    @Value("\${spotify.client-secret}")
    private val clientSecret: String
) {

    private var accessToken: String? = null
    private var tokenExpireAt: LocalDateTime? = null

    companion object {
        private val log = LoggerFactory.getLogger(SpotifyClient::class.java)
    }

    @PostConstruct
    fun init() {
        updateAccessToken()
    }

    fun searchTrack(keyword: String): SpotifySearchResponse? {
        if (tokenExpireAt == null || LocalDateTime.now().isAfter(tokenExpireAt)) {
            log.info("Spotify accessToken 만료됨. 선제 갱신.")
            updateAccessToken()
        }

        return try {
            requestSpotify(keyword)
        } catch (e: Unauthorized) {
            log.warn("Unauthorized 발생. 강제 갱신 후 재시도.")
            updateAccessToken()
            requestSpotify(keyword)
        } catch (e: Exception) {
            log.error("Spotify 응답 파싱 실패", e)
            throw RuntimeException("Spotify API 응답 파싱 실패")
        }
    }

    private fun getAccessToken(): String {
        if (accessToken == null || tokenExpireAt == null || LocalDateTime.now().isAfter(tokenExpireAt)) {
            updateAccessToken()
        }
        return accessToken!!
    }

    private fun updateAccessToken() {
        val response = fetchAccessToken()
        this.accessToken = response.accessToken
        this.tokenExpireAt = LocalDateTime.now().plusSeconds((response.expiresIn - 10).toLong())
        log.info("Spotify accessToken 갱신 성공, 만료 시각: {}", tokenExpireAt)
    }

    private fun fetchAccessToken(): SpotifyTokenResponse {
        val credentials = "$clientId:$clientSecret"
        val encodedCredentials = Base64.getEncoder().encodeToString(credentials.toByteArray())

        val headers = HttpHeaders().apply {
            set("Authorization", "Basic $encodedCredentials")
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        val body = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "client_credentials")
        }

        val request = HttpEntity(body, headers)

        return try {
            val response = restTemplate.postForEntity(
                "https://accounts.spotify.com/api/token",
                request,
                SpotifyTokenResponse::class.java
            )
            response.body!!
        } catch (e: Exception) {
            log.error("Spotify access token 요청 실패", e)
            throw RuntimeException("Spotify API 연동 실패")
        }
    }

    private fun requestSpotify(keyword: String): SpotifySearchResponse? {
        val url = "https://api.spotify.com/v1/search?q=" +
                UriUtils.encode(keyword, StandardCharsets.UTF_8) +
                "&type=track&limit=5"

        val headers = HttpHeaders().apply {
            setBearerAuth(accessToken!!)
        }
        val request = HttpEntity<Void>(headers)

        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            request,
            SpotifySearchResponse::class.java
        )

        return response.body
    }

    fun fetchGenresByArtistId(artistId: String): List<String?> {
        val url = "https://api.spotify.com/v1/artists/$artistId"

        val headers = HttpHeaders().apply {
            setBearerAuth(getAccessToken())
        }
        var request = HttpEntity<Void>(headers)

        return try {
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                Map::class.java
            )
            response.body?.get("genres") as? List<String?> ?: emptyList()
        } catch (e: Unauthorized) {
            updateAccessToken()
            headers.setBearerAuth(getAccessToken())
            request = HttpEntity(headers)

            val retry = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                Map::class.java
            )
            retry.body?.get("genres") as? List<String?> ?: emptyList()
        } catch (e: Exception) {
            throw RuntimeException("Spotify 아티스트 장르 조회 중 오류 발생", e)
        }
    }

    fun getTrackById(id: String): TrackItem? {
        val url = "https://api.spotify.com/v1/tracks/$id"

        val headers = HttpHeaders().apply {
            setBearerAuth(getAccessToken())
        }
        val entity = HttpEntity<Any>(headers)

        return try {
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map::class.java
            )
            val body = response.body
            val mapper = ObjectMapper()
            mapper.convertValue(body, TrackItem::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Spotify 트랙 조회 중 오류 발생", e)
        }
    }
}
