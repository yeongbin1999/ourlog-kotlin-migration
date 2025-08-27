package com.back.ourlog.global.security.jwt;

import com.back.ourlog.global.exception.ErrorCode;
import com.back.ourlog.global.security.exception.JwtAuthenticationException;
import com.back.ourlog.global.security.service.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Component
@Getter
public class JwtProvider {

    private final Key key;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") Duration accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") Duration refreshTokenExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // AccessToken 생성
    public String createAccessToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration.toMillis());

        return Jwts.builder()
                .setSubject(userDetails.getId().toString())
                .claim("nickname", userDetails.getNickname())
                .claim("role", userDetails.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // RefreshToken 생성
    public String createRefreshToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration.toMillis());

        return Jwts.builder()
                .setSubject(userDetails.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 userId 추출
    public String getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    // 토큰 파싱과 검증
    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException(ErrorCode.AUTH_EXPIRED_TOKEN, e);
        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new JwtAuthenticationException(ErrorCode.AUTH_INVALID_TOKEN, e);
        } catch (JwtException e) {
            throw new JwtAuthenticationException(ErrorCode.AUTH_UNAUTHORIZED, e);
        }
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException | JwtAuthenticationException e) {
            return false;
        }
    }

}