package com.back.ourlog.global.security.jwt

import com.back.ourlog.global.exception.ErrorCode
import com.back.ourlog.global.security.exception.JwtAuthenticationException
import com.back.ourlog.global.security.service.CustomUserDetails
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.Key
import java.time.Duration
import java.util.*

@Component
class JwtProvider(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.access-token-expiration}") val accessTokenExpiration: Duration,
    @Value("\${jwt.refresh-token-expiration}") val refreshTokenExpiration: Duration
) {
    private val key: Key = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    // AccessToken 생성
    fun createAccessToken(userDetails: CustomUserDetails): String {
        val now = Date()
        val expiryDate = Date(now.time + accessTokenExpiration.toMillis())

        return Jwts.builder()
            .setSubject(userDetails.id.toString())
            .claim("nickname", userDetails.nickname)
            .claim("role", userDetails.role.name)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    // RefreshToken 생성
    fun createRefreshToken(userDetails: CustomUserDetails): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshTokenExpiration.toMillis())

        return Jwts.builder()
            .setSubject(userDetails.id.toString())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    // 토큰에서 userId 추출
    fun getUserIdFromToken(token: String): String =
        parseClaims(token).subject

    // 토큰 파싱과 검증
    fun parseClaims(token: String): Claims = try {
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    } catch (e: ExpiredJwtException) {
        throw JwtAuthenticationException(ErrorCode.AUTH_EXPIRED_TOKEN, e)
    } catch (e: JwtException) {
        throw JwtAuthenticationException(ErrorCode.AUTH_INVALID_TOKEN, e)
    }

    // 토큰 유효성 검사
    fun validateToken(token: String): Boolean = try {
        parseClaims(token)
        true
    } catch (_: JwtAuthenticationException) {
        false
    }
}