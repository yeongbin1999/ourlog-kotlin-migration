package com.back.ourlog.domain.auth.dto;

public record LoginRequest(
        String email,
        String password
) {}
