package com.back.ourlog.external.spotify.client

import com.back.ourlog.external.spotify.dto.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.Base64

@Component
class SpotifyClient(
    @Qualifier("spotifyAuthApiClient") private val authClient: WebClient,
    @Qualifier("spotifyApiDataClient") private val dataClient: WebClient,
    @Value("\${spotify.client-id}") private val clientId: String,
    @Value("\${spotify.client-secret}") private val clientSecret: String
) {

    private var accessToken: String? = null
    private var tokenExpireAt: LocalDateTime? = null
    private val tokenLock = Any() // 토큰 갱신을 위한 락 객체

    companion object {
        private val log = LoggerFactory.getLogger(SpotifyClient::class.java)
    }

    private fun getAccessToken(): String {
        // 락을 걸기 전에 먼저 토큰이 유효한지 확인 (성능 최적화)
        if (isTokenInvalid()) {
            // 여러 스레드가 동시에 토큰을 갱신하는 것을 방지
            synchronized(tokenLock) {
                // 락을 획득한 후, 다른 스레드가 이미 토큰을 갱신했는지 다시 확인
                if (isTokenInvalid()) {
                    updateAccessToken()
                }
            }
        }
        return accessToken!!
    }

    private fun isTokenInvalid(): Boolean {
        return accessToken == null || tokenExpireAt == null || LocalDateTime.now().isAfter(tokenExpireAt)
    }

    private fun updateAccessToken() {
        log.info("Spotify accessToken 갱신 시도.")
        val response = fetchAccessToken()
        this.accessToken = response.accessToken
        // 만료 시간 10초 전에 갱신하도록 설정
        this.tokenExpireAt = LocalDateTime.now().plusSeconds((response.expiresIn - 10).toLong())
        log.info("Spotify accessToken 갱신 성공, 만료 시각: {}", tokenExpireAt)
    }

    private fun fetchAccessToken(): SpotifyTokenResponse {
        val credentials = "$clientId:$clientSecret"
        val encodedCredentials = Base64.getEncoder().encodeToString(credentials.toByteArray())

        return authClient.post()
            .uri("/api/token")
            .header(HttpHeaders.AUTHORIZATION, "Basic $encodedCredentials")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("grant_type=client_credentials")
            .retrieve()
            .bodyToMono<SpotifyTokenResponse>()
            .block() ?: throw RuntimeException("Spotify access token 요청 실패")
    }

    fun searchTrack(keyword: String): SpotifySearchResponse? {
        return dataClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/v1/search")
                    .queryParam("q", keyword)
                    .queryParam("type", "track")
                    .queryParam("limit", 5)
                    .build()
            }
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${getAccessToken()}")
            .retrieve()
            .bodyToMono<SpotifySearchResponse>()
            .onErrorResume { e ->
                log.error("Spotify 트랙 검색 실패: ", e)
                Mono.empty()
            }
            .block()
    }

    fun fetchGenresByArtistId(artistId: String): List<String> {
        val response = dataClient.get()
            .uri("/v1/artists/{id}", artistId) // Path Variable을 안전하게 사용
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${getAccessToken()}")
            .retrieve()
            .bodyToMono<SpotifyArtist>()
            .onErrorResume { e ->
                log.error("Spotify 아티스트 장르 조회 실패: ", e)
                Mono.empty()
            }
            .block()

        return response?.genres ?: emptyList()
    }

    fun getTrackById(id: String): TrackItem? {
        return dataClient.get()
            .uri("/v1/tracks/{id}", id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${getAccessToken()}")
            .retrieve()
            .bodyToMono<TrackItem>()
            .onErrorResume { e ->
                log.error("Spotify 트랙 ID 조회 실패: ", e)
                Mono.empty()
            }
            .block()
    }
}
