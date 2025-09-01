package com.back.ourlog.domain.follow.controller

import com.back.ourlog.domain.follow.dto.FollowUserResponse
import com.back.ourlog.domain.follow.service.FollowService
import com.back.ourlog.global.common.dto.RsData
import com.back.ourlog.global.common.extension.toSuccessResponse
import com.back.ourlog.global.common.extension.toSuccessResponseWithoutData
import com.back.ourlog.global.rq.Rq
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/follows")
@Tag(name = "팔로우 API")
class FollowController( // 1. 생성자 주입 사용 (Lombok 제거)
    private val followService: FollowService,
    private val rq: Rq
) {

    @PostMapping("/{followeeId}")
    @Operation(summary = "유저 팔로우")
    fun followUser(@PathVariable followeeId: Int): ResponseEntity<RsData<Nothing>> {
        val follower = rq.currentUser
        followService.follow(follower.id!!, followeeId)
        return toSuccessResponseWithoutData("팔로우 요청했습니다.") // 3. 반환값이 없는 경우 처리
    }

    @DeleteMapping("/{otherUserId}")
    @Operation(summary = "팔로우 관계 끊기 (언팔로우)")
    fun unfollowUser(@PathVariable otherUserId: Int): ResponseEntity<RsData<Nothing>> {
        val me = rq.currentUser
        followService.unfollow(me.id!!, otherUserId)
        return toSuccessResponseWithoutData("팔로우 관계를 끊었습니다.")
    }

    // 2. toSuccessResponse 확장 함수 적극 활용
    @GetMapping("/followings")
    @Operation(summary = "내가 팔로우한 유저 목록 조회", description = "ACCEPTED 상태의 목록을 반환합니다.")
    fun getFollowings(): ResponseEntity<RsData<List<FollowUserResponse>>> {
        val me = rq.currentUser
        return followService.getFollowings(me.id!!)
            .toSuccessResponse("내가 팔로우한 유저 목록 조회 완료")
    }

    @GetMapping("/followers")
    @Operation(summary = "나를 팔로우한 유저 목록 조회", description = "ACCEPTED 상태의 목록을 반환합니다.")
    fun getFollowers() = followService.getFollowers(rq.currentUser.id!!) // 4. 코틀린 스타일로 단일 표현식 함수 사용
        .toSuccessResponse("나를 팔로우한 유저 목록 조회 완료")


    @PostMapping("/{followId}/accept")
    @Operation(summary = "팔로우 요청 수락")
    fun acceptFollow(@PathVariable followId: Int) =
        toSuccessResponseWithoutData("팔로우 요청 수락 완료!").also {
            followService.acceptFollow(followId)
        }

    @DeleteMapping("/{followId}/reject")
    @Operation(summary = "팔로우 요청 거절")
    fun rejectFollow(@PathVariable followId: Int): ResponseEntity<RsData<Nothing>> {
        followService.rejectFollow(followId)
        return toSuccessResponseWithoutData("팔로우 요청 거절 완료!")
    }

    @GetMapping("/sent-requests")
    @Operation(summary = "내가 보낸 팔로우 요청 목록 조회", description = "PENDING 상태의 목록을 반환합니다.")
    fun getSentRequests() = followService.getSentPendingRequests(rq.currentUser.id!!)
        .toSuccessResponse("내가 보낸 팔로우 요청 목록 조회 완료")

    @GetMapping("/requests")
    @Operation(summary = "내가 받은 팔로우 요청 목록 조회", description = "PENDING 상태의 목록을 반환합니다.")
    fun getPendingRequests() = followService.getPendingRequests(rq.currentUser.id!!)
        .toSuccessResponse("내가 받은 팔로우 요청 목록 조회 완료")
}