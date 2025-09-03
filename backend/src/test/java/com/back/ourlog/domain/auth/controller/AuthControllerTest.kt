package com.back.ourlog.domain.auth.controller

import com.back.ourlog.domain.auth.dto.LoginRequest
import com.back.ourlog.domain.auth.dto.SignupRequest
import com.back.ourlog.global.security.jwt.TokenDto
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Duration

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    private val testEmail = "test@example.com"
    private val testPassword = "password"
    private val testNickname = "nickname"
    private val testLoginEmail = "user1@test.com"
    private val testLoginPassword = "password1"
    private val deviceId = "device123"

    @BeforeEach
    fun setupTestUser() {
        // 테스트용 계정이 없다면 생성
        val signupRequest = SignupRequest(
            email = testEmail,
            password = testPassword,
            nickname = testNickname,
            profileImageUrl = null,
            bio = null
        )

        mockMvc.post("/api/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(signupRequest)
        }
    }

    @Test
    @DisplayName("회원가입 성공")
    fun signupSuccess() {
        val request = SignupRequest(
            email = "newuser@example.com",
            password = "newpass",
            nickname = "newnick",
            profileImageUrl = null,
            bio = null
        )

        mockMvc.post("/api/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.msg") { value("회원가입 성공") }
        }
    }

    @Test
    @DisplayName("로그인 성공")
    fun loginSuccess() {
        performLoginAndGetTokens(testLoginEmail, testLoginPassword)

        mockMvc.post("/api/v1/auth/login") {
            header("X-Device-Id", deviceId)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(LoginRequest(testLoginEmail, testLoginPassword))
        }.andExpect {
            status { isOk() }
            header { string("Set-Cookie", containsString("refreshToken=")) }
            jsonPath("$.data.accessToken") { exists() }
            jsonPath("$.msg") { value("로그인 성공") }
        }
    }

    @Test
    @DisplayName("로그아웃 성공")
    fun logoutSuccess() {
        val accessToken = obtainAccessToken(testEmail, testPassword)

        mockMvc.post("/api/v1/auth/logout") {
            header("Authorization", "Bearer $accessToken")
            header("X-Device-Id", deviceId)
        }.andExpect {
            status { isOk() }
            header { string("Set-Cookie", containsString("refreshToken=;")) }
            jsonPath("$.msg") { value("로그아웃 성공") }
        }
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    fun reissueSuccess() {
        val tokens = performLoginAndGetTokens(testLoginEmail, testLoginPassword)

        mockMvc.post("/api/v1/auth/reissue") {
            cookie(Cookie("refreshToken", tokens.refreshToken))
            header("X-Device-Id", deviceId)
        }.andExpect {
            status { isOk() }
            header { string("Set-Cookie", containsString("refreshToken=")) }
            jsonPath("$.data.accessToken") { exists() }
            jsonPath("$.msg") { value("토큰 재발급 성공") }
        }
    }

    // ---------- Helper Methods ----------
    private fun obtainAccessToken(email: String, password: String): String {
        val request = LoginRequest(email, password)
        val result = mockMvc.post("/api/v1/auth/login") {
            header("X-Device-Id", deviceId)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andReturn()

        val responseBody = result.response.contentAsString
        return objectMapper.readTree(responseBody).path("data").path("accessToken").asText()
    }

    private fun performLoginAndGetTokens(email: String, password: String): TokenDto {
        val request = LoginRequest(email, password)
        val result = mockMvc.post("/api/v1/auth/login") {
            header("X-Device-Id", deviceId)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andReturn()

        val responseBody = result.response.contentAsString
        val accessToken = objectMapper.readTree(responseBody).path("data").path("accessToken").asText()

        val refreshToken = result.response.getHeader("Set-Cookie")
            ?.substringAfter("refreshToken=")
            ?.substringBefore(";")
            ?: throw IllegalStateException("Refresh token is null or empty after login!")

        return TokenDto(accessToken, refreshToken, Duration.ofDays(7))
    }
}