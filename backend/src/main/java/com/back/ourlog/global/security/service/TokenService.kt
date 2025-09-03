package com.back.ourlog.global.security.service

import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import com.back.ourlog.global.security.jwt.JwtProvider
import com.back.ourlog.global.security.jwt.RefreshTokenRepository
import com.back.ourlog.global.security.jwt.TokenDto
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val jwtProvider: JwtProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val customUserDetailsService: CustomUserDetailsService
) {

    fun issueTokens(userDetails: CustomUserDetails, deviceId: String): TokenDto {
        val accessToken = jwtProvider.createAccessToken(userDetails)
        val refreshToken = jwtProvider.createRefreshToken(userDetails)

        refreshTokenRepository.save(
            userDetails.id.toString(),
            deviceId,
            refreshToken,
            jwtProvider.refreshTokenExpiration
        )

        return TokenDto(accessToken, refreshToken, jwtProvider.refreshTokenExpiration)
    }

    fun reIssueTokens(presentedRefreshToken: String, deviceId: String): TokenDto {
        require(jwtProvider.validateToken(presentedRefreshToken)) {
            throw CustomException(ErrorCode.AUTH_EXPIRED_TOKEN)
        }

        val userId = jwtProvider.getUserIdFromToken(presentedRefreshToken)
        val userDetails = customUserDetailsService.loadUserById(userId)
        val newAccessToken = jwtProvider.createAccessToken(userDetails)
        val newRefreshToken = jwtProvider.createRefreshToken(userDetails)

        val result = refreshTokenRepository.rotateRefreshToken(
            userId,
            deviceId,
            presentedRefreshToken,
            newRefreshToken,
            jwtProvider.refreshTokenExpiration
        )

        check(result == 1L) { throw CustomException(ErrorCode.AUTH_INVALID_TOKEN) }

        return TokenDto(newAccessToken, newRefreshToken, jwtProvider.refreshTokenExpiration)
    }

    fun logout(userId: Int, deviceId: String) {
        refreshTokenRepository.delete(userId.toString(), deviceId)
        // 필요하면 Access 토큰 블랙리스트 처리 추가 가능
    }

    fun extractUserId(accessToken: String): Int {
        require(accessToken.isNotBlank()) {
            throw CustomException(ErrorCode.AUTH_INVALID_TOKEN)
        }
        return jwtProvider.getUserIdFromToken(accessToken).toInt()
    }
}