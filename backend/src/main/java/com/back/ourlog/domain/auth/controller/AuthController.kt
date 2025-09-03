package com.back.ourlog.domain.auth.controller

import com.back.ourlog.domain.auth.dto.LoginRequest
import com.back.ourlog.domain.auth.dto.LoginResponse
import com.back.ourlog.domain.auth.dto.OAuthCallbackRequest
import com.back.ourlog.domain.auth.dto.SignupRequest
import com.back.ourlog.domain.auth.service.AuthService
import com.back.ourlog.domain.auth.service.OAuthService
import com.back.ourlog.global.common.dto.RsData
import com.back.ourlog.global.common.extension.toSuccessResponse
import com.back.ourlog.global.common.extension.toSuccessResponseWithoutData
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val oauthService: OAuthService,
    @Value("\${cookie.secure}") private val secure: Boolean
) {

    @PostMapping("/signup")
    fun signup(@RequestBody request: SignupRequest): ResponseEntity<RsData<Nothing>> {
        authService.signup(request)
        return toSuccessResponseWithoutData("회원가입 성공")
    }

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        @RequestHeader("X-Device-Id") deviceId: String,
        response: HttpServletResponse
    ): ResponseEntity<RsData<LoginResponse>> {
        val tokenDto = authService.login(request.email, request.password, deviceId)
        setRefreshTokenCookie(response, tokenDto.refreshToken, tokenDto.refreshTokenExpiration.seconds)
        return LoginResponse(tokenDto.accessToken).toSuccessResponse("로그인 성공")
    }

    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") authorization: String,
        @RequestHeader("X-Device-Id") deviceId: String,
        response: HttpServletResponse
    ): ResponseEntity<RsData<Nothing>> {
        val accessToken = authorization.removePrefix("Bearer ")
        authService.logout(accessToken, deviceId)
        deleteRefreshTokenCookie(response)
        return toSuccessResponseWithoutData("로그아웃 성공")
    }

    @PostMapping("/reissue")
    fun reissue(
        @CookieValue("refreshToken") refreshToken: String,
        @RequestHeader("X-Device-Id") deviceId: String,
        response: HttpServletResponse
    ): ResponseEntity<RsData<LoginResponse>> {
        val newTokens = authService.reissue(refreshToken, deviceId)
        setRefreshTokenCookie(response, newTokens.refreshToken, newTokens.refreshTokenExpiration.seconds)
        return LoginResponse(newTokens.accessToken).toSuccessResponse("토큰 재발급 성공")
    }

    @PostMapping("/oauth/callback/{provider}")
    fun oauthCallback(
        @PathVariable provider: String,
        @RequestBody request: OAuthCallbackRequest,
        @RequestHeader("X-Device-Id") deviceId: String,
        response: HttpServletResponse
    ) = kotlinx.coroutines.runBlocking {
        val tokenDto = oauthService.handleOAuthCallback(provider, request, deviceId)
        setRefreshTokenCookie(response, tokenDto.refreshToken, tokenDto.refreshTokenExpiration.seconds)
        LoginResponse(tokenDto.accessToken).toSuccessResponse("OAuth 로그인 성공")
    }

    private fun setRefreshTokenCookie(response: HttpServletResponse, token: String, maxAge: Long) {
        val cookie = ResponseCookie.from("refreshToken", token)
            .httpOnly(true)
            .secure(secure)
            .path("/")
            .maxAge(maxAge)
            .sameSite("Strict")
            .build()
        response.addHeader("Set-Cookie", cookie.toString())
    }

    private fun deleteRefreshTokenCookie(response: HttpServletResponse) {
        val cookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(secure)
            .path("/")
            .maxAge(0)
            .sameSite("Strict")
            .build()
        response.addHeader("Set-Cookie", cookie.toString())
    }
}