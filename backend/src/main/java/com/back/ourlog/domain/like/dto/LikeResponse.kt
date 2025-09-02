package com.back.ourlog.domain.like.dto

data class LikeResponse(
    val liked: Boolean,
    val likeCount: Int
)