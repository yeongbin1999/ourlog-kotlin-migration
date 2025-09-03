package com.back.ourlog.domain.auth.dto

data class SignupRequest(
    val email: String,
    val password: String,
    val nickname: String,
    val profileImageUrl: String?,
    val bio: String?
)