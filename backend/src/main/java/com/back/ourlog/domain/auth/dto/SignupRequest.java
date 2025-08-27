package com.back.ourlog.domain.auth.dto;

public record SignupRequest(
        String email,
        String password,
        String nickname,
        String profileImageUrl,
        String bio
) {}
