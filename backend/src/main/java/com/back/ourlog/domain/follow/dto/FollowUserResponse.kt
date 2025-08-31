package com.back.ourlog.domain.follow.dto

import com.back.ourlog.domain.follow.entity.Follow

data class FollowUserResponse(
    val userId: Int,
    val email: String,
    val nickname: String,
    val profileImageUrl: String?, // 프로필 이미지는 Null일 수 있음을 명시
    val followId: Int,
    val isFollowing: Boolean
) {
    companion object {
        fun fromFollower(follow: Follow, isFollowing: Boolean = true): FollowUserResponse {
            val follower = follow.follower
            return FollowUserResponse(
                userId = follower.id!!,
                email = follower.email,
                nickname = follower.nickname,
                profileImageUrl = follower.profileImageUrl,
                followId = follow.id,
                isFollowing = isFollowing
            )
        }

        fun fromFollowee(follow: Follow, isFollowing: Boolean = true): FollowUserResponse {
            val followee = follow.followee
            return FollowUserResponse(
                userId = followee.id!!,
                email = followee.email,
                nickname = followee.nickname,
                profileImageUrl = followee.profileImageUrl,
                followId = follow.id,
                isFollowing = isFollowing
            )
        }
    }
}