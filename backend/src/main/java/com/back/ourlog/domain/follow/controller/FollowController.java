package com.back.ourlog.domain.follow.controller;

import com.back.ourlog.domain.follow.dto.FollowUserResponse;
import com.back.ourlog.domain.follow.service.FollowService;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.global.exception.CustomException;
import com.back.ourlog.global.exception.ErrorCode;
import com.back.ourlog.global.rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/follows")
@Tag(name = "팔로우 API")
public class FollowController {

    private final FollowService followService;
    private final Rq rq;

    @PostMapping("/{followeeId}")
    @Operation(summary = "유저 팔로우")
    public ResponseEntity<String> followUser(@PathVariable Integer followeeId) {
        User follower = rq.getCurrentUser();

        followService.follow(follower.getId(), followeeId);
        return ResponseEntity.ok("팔로우 요청했습니다.");
    }

    @DeleteMapping("/{otherUserId}")
    @Operation(summary = "팔로우 관계 끊기 (언팔로우)")
    public ResponseEntity<String> unfollowUser(@PathVariable Integer otherUserId) {
        User me = rq.getCurrentUser();

        followService.unfollow(me.getId(), otherUserId);
        return ResponseEntity.ok("팔로우 관계를 끊었습니다.");
    }

    @GetMapping("/followings")
    @Operation(summary = "내가 팔로우한 유저 목록 조회", description = "ACCEPTED 상태의 목록을 반환합니다.")
    public ResponseEntity<List<FollowUserResponse>> getFollowings() {
        User me = rq.getCurrentUser();

        List<FollowUserResponse> response = followService.getFollowings(me.getId());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/followers")
    @Operation(summary = "나를 팔로우한 유저 목록 조회", description = "ACCEPTED 상태의 목록을 반환합니다.")
    public ResponseEntity<List<FollowUserResponse>> getFollowers() {
        User me = rq.getCurrentUser();

        List<FollowUserResponse> response = followService.getFollowers(me.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{followId}/accept")
    @Operation(summary = "팔로우 요청 수락")
    public ResponseEntity<String> acceptFollow(@PathVariable Integer followId) {
        // 팔로우 ID는 그대로 받고, 로그인 체크만

        followService.acceptFollow(followId);
        return ResponseEntity.ok("팔로우 요청 수락 완료!");
    }

    @DeleteMapping("/{followId}/reject")
    @Operation(summary = "팔로우 요청 거절")
    public ResponseEntity<String> rejectFollow(@PathVariable Integer followId) {

        followService.rejectFollow(followId);
        return ResponseEntity.ok("팔로우 요청 거절 완료!");
    }

    @GetMapping("/sent-requests")
    @Operation(summary = "내가 보낸 팔로우 요청 목록 조회", description = "아직 수락되지 않은 PENDING 상태의 팔로우 요청 목록을 반환합니다.")
    public ResponseEntity<List<FollowUserResponse>> getSentRequests() {
        User me = rq.getCurrentUser();

        List<FollowUserResponse> response = followService.getSentPendingRequests(me.getId());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/requests")
    @Operation(summary = "내가 받은 팔로우 요청 목록 조회", description = "PENDING 상태의 팔로우 요청 목록을 반환합니다.")
    public ResponseEntity<List<FollowUserResponse>> getPendingRequests() {
        User me = rq.getCurrentUser();

        List<FollowUserResponse> response = followService.getPendingRequests(me.getId());
        return ResponseEntity.ok(response);
    }



}
