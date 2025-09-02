package com.back.ourlog.domain.auth.service

import com.back.ourlog.domain.auth.dto.OAuthCallbackRequest
import com.back.ourlog.global.security.jwt.TokenDto
import com.back.ourlog.global.security.oauth.OAuthAttributes
import com.back.ourlog.global.security.service.CustomUserDetails
import com.back.ourlog.global.security.service.TokenService
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

@Service
class OAuthService(
    private val webClientBuilder: WebClient.Builder,
    private val tokenService: TokenService,
    private val authService: AuthService,
    @Value("\${oauth2.google.client-id}") private val googleClientId: String,
    @Value("\${oauth2.google.client-secret}") private val googleClientSecret: String,
    @Value("\${oauth2.google.token-uri}") private val googleTokenUri: String,
    @Value("\${oauth2.google.user-info-uri}") private val googleUserInfoUri: String,
    @Value("\${oauth2.kakao.client-id}") private val kakaoClientId: String,
    @Value("\${oauth2.kakao.client-secret}") private val kakaoClientSecret: String,
    @Value("\${oauth2.kakao.token-uri}") private val kakaoTokenUri: String,
    @Value("\${oauth2.kakao.user-info-uri}") private val kakaoUserInfoUri: String,
    @Value("\${oauth2.naver.client-id}") private val naverClientId: String,
    @Value("\${oauth2.naver.client-secret}") private val naverClientSecret: String,
    @Value("\${oauth2.naver.token-uri}") private val naverTokenUri: String,
    @Value("\${oauth2.naver.user-info-uri}") private val naverUserInfoUri: String
) {

    private val webClient: WebClient by lazy { webClientBuilder.build() }

    // 최종 엔트리
    suspend fun handleOAuthCallback(
        provider: String,
        request: OAuthCallbackRequest,
        deviceId: String
    ): TokenDto {
        val accessToken = getAccessToken(provider.lowercase(), request)
        val rawAttributes = getUserAttributes(provider.lowercase(), accessToken)
        val oAuthAttributes = OAuthAttributes.of(provider.lowercase(), rawAttributes)

        val userDetails: CustomUserDetails = authService.findOrCreateOAuthUser(
            providerId = oAuthAttributes.providerId,
            email = oAuthAttributes.email,
            nickname = oAuthAttributes.name,
            profileImageUrl = oAuthAttributes.attributes["profileImageUrl"] as? String,
            provider = provider
        )

        return tokenService.issueTokens(userDetails, deviceId)
    }

    private suspend fun getAccessToken(provider: String, request: OAuthCallbackRequest): String {
        val (tokenUri, clientId, clientSecret, codeVerifier) = when (provider) {
            "google" -> Quad(googleTokenUri, googleClientId, googleClientSecret, request.codeVerifier)
            "kakao" -> Quad(kakaoTokenUri, kakaoClientId, kakaoClientSecret, null)
            "naver" -> Quad(naverTokenUri, naverClientId, naverClientSecret, null)
            else -> error("Unsupported OAuth provider: $provider")
        }

        val formData = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", clientId)
            add("client_secret", clientSecret)
            add("redirect_uri", request.redirectUri)
            add("code", request.code)
            codeVerifier?.let { add("code_verifier", it) }
        }

        val response = webClient.post()
            .uri(tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .bodyToMono(Map::class.java)
            .awaitSingle()

        return response["access_token"] as? String ?: error("Failed to get access token")
    }

    private suspend fun getUserAttributes(provider: String, accessToken: String): Map<String, Any> {
        val userInfoUri = when (provider) {
            "google" -> googleUserInfoUri
            "kakao" -> kakaoUserInfoUri
            "naver" -> naverUserInfoUri
            else -> error("Unknown provider: $provider")
        }

        return webClient.get()
            .uri(userInfoUri)
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .bodyToMono(Map::class.java)
            .awaitSingle() as Map<String, Any>
    }

    // 간단한 구조체용 데이터 클래스
    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D?)
}