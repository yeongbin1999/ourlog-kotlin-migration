package com.back.ourlog.domain.follow.controller

import com.back.ourlog.domain.follow.dto.FollowStatusResponse
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
class FollowController(
    private val followService: FollowService,
    private val rq: Rq
) {

    @PostMapping("/{followeeId}")
    @Operation(summary = "유저 팔로우")
    fun followUser(@PathVariable followeeId: Int): ResponseEntity<RsData<Nothing>> {
        followService.follow(rq.currentUser.id, followeeId)
        return toSuccessResponseWithoutData("팔로우 요청했습니다.")
    }

    @DeleteMapping("/{followeeId}/cancel")
    @Operation(summary = "팔로우 요청 취소")
    fun cancelFollowRequest(@PathVariable followeeId: Int): ResponseEntity<RsData<Nothing>> {
        followService.cancelFollowRequest(rq.currentUser.id, followeeId)
        return toSuccessResponseWithoutData("팔로우 요청을 취소했습니다.")
    }

    @DeleteMapping("/{otherUserId}")
    @Operation(summary = "팔로우 관계 끊기 (언팔로우)")
    fun unfollowUser(@PathVariable otherUserId: Int): ResponseEntity<RsData<Nothing>> {
        followService.unfollow(rq.currentUser.id, otherUserId)
        return toSuccessResponseWithoutData("팔로우 관계를 끊었습니다.")
    }

    @GetMapping("/followings")
    @Operation(summary = "내가 팔로우한 유저 목록 조회", description = "ACCEPTED 상태의 목록을 반환합니다.")
    fun getFollowings(): ResponseEntity<RsData<List<FollowUserResponse>>> =
        followService.getFollowings(rq.currentUser.id)
            .toSuccessResponse("내가 팔로우한 유저 목록 조회 완료")

    @GetMapping("/followers")
    @Operation(summary = "나를 팔로우한 유저 목록 조회", description = "ACCEPTED 상태의 목록을 반환합니다.")
    fun getFollowers(): ResponseEntity<RsData<List<FollowUserResponse>>> =
        followService.getFollowers(rq.currentUser.id)
            .toSuccessResponse("나를 팔로우한 유저 목록 조회 완료")

    @GetMapping("/status/{otherUserId}")
    @Operation(
        summary = "특정 유저와의 팔로우 상태 조회",
        description = "나와 특정 유저의 팔로우 상태와 followId를 반환합니다. NONE / PENDING / ACCEPTED 중 하나"
    )
    fun getFollowStatus(@PathVariable otherUserId: Int): ResponseEntity<RsData<FollowStatusResponse>> {
        val (status, followId) = followService.getFollowStatusWithId(rq.currentUser.id, otherUserId)
        return FollowStatusResponse(status, followId).toSuccessResponse("팔로우 상태 조회 성공")
    }

    @PostMapping("/{followId}/accept")
    @Operation(summary = "팔로우 요청 수락")
    fun acceptFollow(@PathVariable followId: Int): ResponseEntity<RsData<Nothing>> =
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
    fun getSentRequests(): ResponseEntity<RsData<List<FollowUserResponse>>> =
        followService.getSentPendingRequests(rq.currentUser.id)
            .toSuccessResponse("내가 보낸 팔로우 요청 목록 조회 완료")

    @GetMapping("/requests")
    @Operation(summary = "내가 받은 팔로우 요청 목록 조회", description = "PENDING 상태의 목록을 반환합니다.")
    fun getPendingRequests(): ResponseEntity<RsData<List<FollowUserResponse>>> =
        followService.getPendingRequests(rq.currentUser.id)
            .toSuccessResponse("내가 받은 팔로우 요청 목록 조회 완료")
}