package com.back.ourlog.global.security.jwt;

import com.back.ourlog.global.exception.ErrorCode;
import com.back.ourlog.global.security.exception.JwtAuthenticationException;
import com.back.ourlog.global.security.service.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtProviderTest {

    @Autowired
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("AccessToken 생성 및 유효성 검증")
    @WithUserDetails("user1@test.com")
    void createAccessToken_ShouldReturnValidToken() {
        CustomUserDetails userDetails = getCurrentUserDetails();

        String accessToken = jwtProvider.createAccessToken(userDetails);

        assertNotNull(accessToken);
        assertTrue(jwtProvider.validateToken(accessToken));
    }

    @Test
    @DisplayName("RefreshToken 생성 및 유효성 검증")
    @WithUserDetails("user1@test.com")
    void createRefreshToken_ShouldReturnValidToken() {
        CustomUserDetails userDetails = getCurrentUserDetails();

        String refreshToken = jwtProvider.createRefreshToken(userDetails);

        assertNotNull(refreshToken);
        assertTrue(jwtProvider.validateToken(refreshToken));
    }

    @Test
    @DisplayName("AccessToken 내 클레임 포함 확인")
    @WithUserDetails("user1@test.com")
    void shouldIncludeClaimsInAccessToken() {
        CustomUserDetails userDetails = getCurrentUserDetails();

        String token = jwtProvider.createAccessToken(userDetails);
        Claims claims = jwtProvider.parseClaims(token);

        assertEquals(userDetails.getNickname(), claims.get("nickname"));
        assertEquals(userDetails.getRole().name(), claims.get("role"));
    }

    @Test
    @DisplayName("토큰에서 UserId 추출")
    @WithUserDetails("user1@test.com")
    void getUserIdFromToken_ShouldReturnCorrectUserId() {
        CustomUserDetails userDetails = getCurrentUserDetails();

        String accessToken = jwtProvider.createAccessToken(userDetails);

        String extractedUserId = jwtProvider.getUserIdFromToken(accessToken);

        assertEquals(userDetails.getId().toString(), extractedUserId);
    }

    @Test
    @DisplayName("유효하지 않은 토큰은 validateToken()에서 false 반환")
    void validateToken_ShouldReturnFalseForInvalidToken() {
        String invalidToken = "this.is.not.a.valid.token";

        assertFalse(jwtProvider.validateToken(invalidToken));
    }

    @Test
    @DisplayName("만료된 토큰은 parseClaims() 호출 시 JwtAuthenticationException 발생")
    @WithUserDetails("user1@test.com")
    void parseClaims_ShouldThrowExceptionForExpiredToken() {
        CustomUserDetails userDetails = getCurrentUserDetails();

        Date pastIssuedAt = new Date(System.currentTimeMillis() - 10_000_000);
        Date pastExpiration = new Date(System.currentTimeMillis() - 5_000);

        String expiredToken = Jwts.builder()
                .setSubject(userDetails.getId().toString())
                .setIssuedAt(pastIssuedAt)
                .setExpiration(pastExpiration)
                .signWith(jwtProvider.getKey(), SignatureAlgorithm.HS256)
                .compact();

        JwtAuthenticationException exception = assertThrows(JwtAuthenticationException.class,
                () -> jwtProvider.parseClaims(expiredToken));

        assertEquals(ErrorCode.AUTH_EXPIRED_TOKEN, exception.getErrorCode());
    }

    // =================== 유틸 메서드 =======================
    private CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication, "Authentication should not be null");
        assertTrue(authentication.getPrincipal() instanceof CustomUserDetails,
                "Principal should be instance of CustomUserDetails");
        return (CustomUserDetails) authentication.getPrincipal();
    }

}
