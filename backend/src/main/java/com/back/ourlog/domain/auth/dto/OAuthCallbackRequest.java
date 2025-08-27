package com.back.ourlog.domain.auth.dto;

public record OAuthCallbackRequest(
        String code,
        String codeVerifier,
        String redirectUri
) {
}