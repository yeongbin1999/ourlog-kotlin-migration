package com.back.ourlog.global.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    // Spotify 인증 API 호출을 위한 WebClient
    @Bean
    @Qualifier("spotifyAuthApiClient")
    fun spotifyAuthApiClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://accounts.spotify.com")
            .build()
    }

    // Spotify 데이터 API 호출을 위한 WebClient
    @Bean
    @Qualifier("spotifyApiDataClient")
    fun spotifyApiDataClient(): WebClient {
        return WebClient.builder()
            .baseUrl("accounts.spotify.com")
            .build()
    }
}
