package com.back.ourlog.global.security.jwt

import java.time.Duration

data class TokenDto(
    val accessToken: String,
    val refreshToken: String,
    val refreshTokenExpiration: Duration
)