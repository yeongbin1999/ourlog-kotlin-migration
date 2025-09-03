package com.back.ourlog.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    // 공통 WebClient Builder
    @Bean
    fun webClientBuilder(): WebClient.Builder = WebClient.builder()

    // Spotify 인증 API
    @Bean
    fun spotifyAuthApiClient(webClientBuilder: WebClient.Builder): WebClient =
        webClientBuilder
            .baseUrl("https://accounts.spotify.com")
            .build()

    // Spotify 데이터 API
    @Bean
    fun spotifyApiDataClient(webClientBuilder: WebClient.Builder): WebClient =
        webClientBuilder
            .baseUrl("https://api.spotify.com")
            .build()

    // 국립중앙도서관 API
    @Bean
    fun libraryWebClient(webClientBuilder: WebClient.Builder): WebClient =
        webClientBuilder
            .baseUrl("https://www.nl.go.kr")
            .build()
}