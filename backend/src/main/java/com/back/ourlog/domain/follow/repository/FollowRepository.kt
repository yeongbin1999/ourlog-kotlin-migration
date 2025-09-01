package com.back.ourlog.domain.follow.repository

import com.back.ourlog.domain.follow.entity.Follow
import com.back.ourlog.domain.follow.enums.FollowStatus
import org.springframework.data.jpa.repository.JpaRepository

interface FollowRepository : JpaRepository<Follow, Int> {

    // 특정 follower가 followee를 팔로우하는 단방향 관계(follower → followee)를 조회합니다.
    fun findByFollowerIdAndFolloweeId(followerId: Int, followeeId: Int): Follow?

    // 특정 상태의 팔로우 관계를 조회하는 메서드는 명확하므로 유지
    fun findByFollowerIdAndFolloweeIdAndStatus(followerId: Int, followeeId: Int, status: FollowStatus): Follow?

    // 내가 팔로우한 유저 목록 (수락된 것만)
    fun findFollowingsByFollowerIdAndStatus(followerId: Int, status: FollowStatus): List<Follow>

    // 나를 팔로우한 유저 목록 (수락된 것만)
    fun findFollowersByFolloweeIdAndStatus(followeeId: Int, status: FollowStatus): List<Follow>

    // 내가 보낸 팔로우 요청 목록
    fun findSentPendingRequestsByFollowerIdAndStatus(followerId: Int, status: FollowStatus): List<Follow>

    // 내가 받은 팔로우 요청 목록
    fun findPendingRequestsByFolloweeIdAndStatus(followeeId: Int, status: FollowStatus): List<Follow>

}