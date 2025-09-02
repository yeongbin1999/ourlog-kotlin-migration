package com.back.ourlog.global.security.jwt

import com.back.ourlog.global.exception.ErrorCode
import com.back.ourlog.global.security.exception.JwtAuthenticationException
import com.back.ourlog.global.security.service.CustomUserDetails
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
class JwtProviderTest @Autowired constructor(
    private val jwtProvider: JwtProvider,
    @Value("\${jwt.secret}") secret: String
) {
    private val key: Key = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))


    @Test
    @DisplayName("AccessToken 생성 및 유효성 검증")
    @WithUserDetails("user1@test.com")
    fun `create access token should return valid token`() {
        val userDetails = currentUserDetails()

        val accessToken = jwtProvider.createAccessToken(userDetails)

        assertNotNull(accessToken)
        assertTrue(jwtProvider.validateToken(accessToken))
    }

    @Test
    @DisplayName("RefreshToken 생성 및 유효성 검증")
    @WithUserDetails("user1@test.com")
    fun `create refresh token should return valid token`() {
        val userDetails = currentUserDetails()

        val refreshToken = jwtProvider.createRefreshToken(userDetails)

        assertNotNull(refreshToken)
        assertTrue(jwtProvider.validateToken(refreshToken))
    }

    @Test
    @DisplayName("AccessToken 내 클레임 포함 확인")
    @WithUserDetails("user1@test.com")
    fun `should include claims in access token`() {
        val userDetails = currentUserDetails()

        val token = jwtProvider.createAccessToken(userDetails)
        val claims: Claims = jwtProvider.parseClaims(token)

        assertEquals(userDetails.nickname, claims["nickname"])
        assertEquals(userDetails.role.name, claims["role"])
    }

    @Test
    @DisplayName("토큰에서 UserId 추출")
    @WithUserDetails("user1@test.com")
    fun `get userId from token should return correct userId`() {
        val userDetails = currentUserDetails()

        val accessToken = jwtProvider.createAccessToken(userDetails)
        val extractedUserId = jwtProvider.getUserIdFromToken(accessToken)

        assertEquals(userDetails.id.toString(), extractedUserId)
    }

    @Test
    @DisplayName("유효하지 않은 토큰은 validateToken()에서 false 반환")
    fun `validate token should return false for invalid token`() {
        val invalidToken = "this.is.not.a.valid.token"
        assertFalse(jwtProvider.validateToken(invalidToken))
    }

    @Test
    @DisplayName("만료된 토큰은 parseClaims() 호출 시 JwtAuthenticationException 발생")
    @WithUserDetails("user1@test.com")
    fun `parse claims should throw exception for expired token`() {
        val userDetails = currentUserDetails()

        val pastIssuedAt = Date(System.currentTimeMillis() - 10_000_000)
        val pastExpiration = Date(System.currentTimeMillis() - 5_000)

        val expiredToken = Jwts.builder()
            .setSubject(userDetails.id.toString())
            .setIssuedAt(pastIssuedAt)
            .setExpiration(pastExpiration)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()

        val exception = assertThrows(JwtAuthenticationException::class.java) {
            jwtProvider.parseClaims(expiredToken)
        }
        assertEquals(ErrorCode.AUTH_EXPIRED_TOKEN, exception.errorCode)
    }

    // =================== 유틸 메서드 =======================
    private fun currentUserDetails(): CustomUserDetails {
        val authentication: Authentication =
            SecurityContextHolder.getContext().authentication ?: error("Authentication should not be null")
        check(authentication.principal is CustomUserDetails) { "Principal should be instance of CustomUserDetails" }
        return authentication.principal as CustomUserDetails
    }
}