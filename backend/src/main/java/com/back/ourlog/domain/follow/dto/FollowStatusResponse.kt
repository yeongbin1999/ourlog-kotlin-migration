package com.back.ourlog.domain.follow.dto

data class FollowStatusResponse(
    val status: String,    // NONE / PENDING / ACCEPTED
    val followId: Int?     // PENDING/ACCEPTED 상태일 때만 followId 반환
)