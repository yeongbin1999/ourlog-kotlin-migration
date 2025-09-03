package com.back.ourlog.domain.user.dto

import com.back.ourlog.domain.user.entity.User

data class MyProfileResponse(
    val userId: Int?,
    val email: String?,
    val nickname: String?,
    val profileImageUrl: String?,
    val bio: String?,
    val followingsCount: Int?,
    val followersCount: Int?
) {
    companion object {
        fun from(user: User): MyProfileResponse {
            return MyProfileResponse(
                user.id,
                user.email,
                user.nickname,
                user.profileImageUrl,
                user.bio,
                user.followingsCount,
                user.followersCount
            )
        }
    }
}