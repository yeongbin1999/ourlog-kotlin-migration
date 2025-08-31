package com.back.ourlog.domain.follow.service

import com.back.ourlog.domain.follow.dto.FollowUserResponse
import com.back.ourlog.domain.follow.entity.Follow
import com.back.ourlog.domain.follow.enums.FollowStatus
import com.back.ourlog.domain.follow.repository.FollowRepository
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.domain.user.repository.UserRepository
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FollowService(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
) {
    // 팔로우 요청을 처리하는 함수
    @Transactional
    fun follow(followerId: Int, followeeId: Int) {
        // require 함수를 사용해 자기 자신을 팔로우하는 요청을 막고, 예외를 발생시킵니다.
        require(followerId != followeeId) { throw CustomException(ErrorCode.CANNOT_FOLLOW_SELF) }

        // 기존에 팔로우 관계가 있는지 두 가지 경우(정방향, 역방향)로 데이터베이스를 조회합니다.
        val existingFollow = followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
        val reversedFollow = followRepository.findByFollowerIdAndFolloweeId(followeeId, followerId)

        // when 구문을 사용해 다양한 상황(이미 팔로우, 맞팔, 신규 요청)을 깔끔하게 처리합니다.
        when {
            // Case 1: 이미 팔로우 중이거나 요청을 보낸 경우
            existingFollow?.status == FollowStatus.ACCEPTED || existingFollow?.status == FollowStatus.PENDING -> {
                throw CustomException(ErrorCode.FOLLOW_ALREADY_EXISTS)
            }

            // Case 2: 상대방이 나에게 보낸 'PENDING' 요청이 있는 경우 -> 맞팔 수락 로직 제거 및 예외 발생
            reversedFollow?.status == FollowStatus.PENDING -> {
                // 즉시 수락하지 않고, 예외를 발생시켜 클라이언트가 '요청 수락' 버튼을 누르도록 유도
                throw CustomException(ErrorCode.FOLLOW_REQUEST_EXISTS)
            }

            // Case 3: 기존 관계가 없거나 'REJECTED' 상태, 상대가 나를 팔로우 중인 경우 모두 포함
            // REJECTED 관계가 있으면 삭제하고, 새로운 PENDING 팔로우 요청을 생성합니다.
            else -> {
                existingFollow?.let { followRepository.delete(it) } // 내가 보냈던 REJECTED 요청 등이 있다면 삭제
                val newFollow = Follow(findUserById(followerId), findUserById(followeeId))
                followRepository.save(newFollow)
                updateFollowCounts(newFollow)
            }
        }
    }

    // 언팔로우 요청을 처리하는 함수
    @Transactional
    fun unfollow(myUserId: Int, otherUserId: Int) {
        // 1. myUserId가 otherUserId를 팔로우하는 '단방향' 관계만 정확히 찾는다
        val followToDelete = followRepository.findByFollowerIdAndFolloweeId(myUserId, otherUserId)
            ?: throw CustomException(ErrorCode.FOLLOW_NOT_FOUND)

        // 2. 해당 관계 '하나'의 팔로워/팔로이 카운트만 감소시킨다
        followToDelete.follower.decreaseFollowingsCount() // 나의 팔로잉 수 감소
        followToDelete.followee.decreaseFollowersCount() // 상대방의 팔로워 수 감소

        // 3. 데이터베이스에서 해당 팔로우 관계 '하나만' 삭제한다
        followRepository.delete(followToDelete)
    }

    // 내가 팔로우한 유저 목록을 조회하는 함수
    fun getFollowings(userId: Int): List<FollowUserResponse> {
        val user = findUserById(userId)
        // findFollowingsByFollowerIdAndStatus 메서드를 호출해 ACCEPTED 상태인 팔로우 목록을 가져옵니다.
        // map 함수를 사용해 각 Follow 객체를 FollowUserResponse DTO로 변환합니다.
        return followRepository.findFollowingsByFollowerIdAndStatus(user.id!!, FollowStatus.ACCEPTED).map {
            FollowUserResponse.fromFollowee(it, true)
        }
    }

    // 나를 팔로우한 유저 목록을 조회하는 함수
    fun getFollowers(userId: Int): List<FollowUserResponse> {
        val user = findUserById(userId)
        // 나를 팔로우하는 유저 목록을 가져옵니다.
        val followers = followRepository.findFollowersByFolloweeIdAndStatus(user.id!!, FollowStatus.ACCEPTED)

        // 내가 팔로우하는 모든 사람의 ID를 Set으로 미리 가져와 메모리에 저장합니다.
        // 이렇게 하면 데이터베이스를 여러 번 조회하지 않아도 됩니다.
        val myFollowingIds = followRepository.findFollowingsByFollowerIdAndStatus(user.id!!, FollowStatus.ACCEPTED).map { it.followee.id }.toSet()

        // map 함수를 사용해 각 팔로워에 대해 내가 맞팔로우하는지 여부를 확인하고 DTO로 변환합니다.
        return followers.map { follow ->
            val isFollowingBack = myFollowingIds.contains(follow.follower.id!!)
            FollowUserResponse.fromFollower(follow, isFollowingBack)
        }
    }

    // 팔로우 요청을 수락하는 함수
    @Transactional
    fun acceptFollow(followId: Int) {
        val follow = findFollowById(followId)
        // when 구문을 사용해 현재 팔로우 상태에 따라 다른 동작을 처리합니다.
        when (follow.status) {
            FollowStatus.ACCEPTED -> throw CustomException(ErrorCode.FOLLOW_ALREADY_ACCEPTED)
            FollowStatus.REJECTED -> throw CustomException(ErrorCode.FOLLOW_ALREADY_REJECTED)
            FollowStatus.PENDING -> {
                // PENDING 상태일 때만 수락 처리합니다.
                follow.accept()
                updateFollowCounts(follow)
            }
        }
    }

    // 팔로우 요청을 거절하는 함수
    @Transactional
    fun rejectFollow(followId: Int) {
        val follow = findFollowById(followId)
        // when 구문을 사용해 현재 팔로우 상태에 따라 다른 동작을 처리합니다.
        when (follow.status) {
            FollowStatus.REJECTED -> throw CustomException(ErrorCode.FOLLOW_ALREADY_REJECTED)
            FollowStatus.ACCEPTED -> throw CustomException(ErrorCode.FOLLOW_ALREADY_ACCEPTED)
            FollowStatus.PENDING -> follow.reject()
        }
    }

    // 팔로워와 팔로이의 팔로우/팔로잉 카운트를 증가시킵니다.
    private fun updateFollowCounts(vararg follows: Follow) {
        follows.forEach { follow ->
            follow.follower.increaseFollowingsCount()
            follow.followee.increaseFollowersCount()
        }
    }

    // 내가 보낸 팔로우 요청 목록을 조회하는 함수
    fun getSentPendingRequests(userId: Int): List<FollowUserResponse> {
        val user = findUserById(userId)
        return followRepository.findSentPendingRequestsByFollowerIdAndStatus(user.id!!, FollowStatus.PENDING)
            .map(FollowUserResponse::fromFollowee)
    }

    // 내가 받은 팔로우 요청 목록을 조회하는 함수
    fun getPendingRequests(userId: Int): List<FollowUserResponse> {
        val user = findUserById(userId)
        return followRepository.findPendingRequestsByFolloweeIdAndStatus(user.id!!, FollowStatus.PENDING)
            .map(FollowUserResponse::fromFollower)
    }

    // 주어진 ID로 유저를 찾아 반환하는 함수
    private fun findUserById(userId: Int): User {
        return userRepository.findByIdOrNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
    }

    // 주어진 ID로 팔로우 관계를 찾아 반환하는 함수
    private fun findFollowById(followId: Int): Follow {
        return followRepository.findByIdOrNull(followId)
            ?: throw CustomException(ErrorCode.FOLLOW_NOT_FOUND)
    }
}
