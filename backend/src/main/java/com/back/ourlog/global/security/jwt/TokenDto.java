package com.back.ourlog.global.security.jwt;

import java.time.Duration;

public record TokenDto (
        String accessToken,
        String refreshToken,
        Duration refreshTokenExpiration
) {}

