package com.back.ourlog.domain.auth.controller;

import com.back.ourlog.domain.auth.dto.LoginRequest;
import com.back.ourlog.domain.auth.dto.SignupRequest;
import com.back.ourlog.global.security.jwt.TokenDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() throws Exception {
        SignupRequest request = new SignupRequest(
                "test@example.com",
                "password",
                "nickname",
                null,
                null
        );

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("회원가입 성공"));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest("user1@test.com", "password1");

        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Device-Id", "device123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("refreshToken=")))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.msg").value("로그인 성공"));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() throws Exception {
        String accessToken = obtainAccessToken();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .header("X-Device-Id", "device123"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("refreshToken=;")))
                .andExpect(jsonPath("$.msg").value("로그아웃 성공"));
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissue_success() throws Exception {
        TokenDto loginTokens = performLoginAndGetTokens();

        mockMvc.perform(post("/api/v1/auth/reissue")
                        .cookie(new Cookie("refreshToken", loginTokens.refreshToken()))
                        .header("X-Device-Id", "device123"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("refreshToken=")))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.msg").value("토큰 재발급 성공"));
    }

    // accessToken만 추출
    private String obtainAccessToken() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "password");

        var result = mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Device-Id", "device123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).path("data").path("accessToken").asText();
    }

    // accessToken + refreshToken 모두 추출 (refreshToken 추출 개선)
    private TokenDto performLoginAndGetTokens() throws Exception {
        LoginRequest request = new LoginRequest("user1@test.com", "password1");

        var result = mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Device-Id", "device123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseBody).path("data").path("accessToken").asText();

        String setCookieHeader = result.getResponse().getHeader("Set-Cookie");
        String refreshToken = null;

        if (setCookieHeader != null) {
            int startIndex = setCookieHeader.indexOf("refreshToken=");
            if (startIndex != -1) {
                int endIndex = setCookieHeader.indexOf(";", startIndex);
                if (endIndex == -1) endIndex = setCookieHeader.length();
                refreshToken = setCookieHeader.substring(startIndex + "refreshToken=".length(), endIndex);
            }
        }

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalStateException("Refresh token is null or empty after login!");
        }

        return new TokenDto(accessToken, refreshToken, Duration.ofDays(7));
    }
}
