package com.back.ourlog.domain.follow.dto;

import com.back.ourlog.domain.follow.entity.Follow;
import com.back.ourlog.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 팔로우 유저 조회 응답 DTO..
@Getter
@AllArgsConstructor
public class FollowUserResponse {
    private Integer userId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private Integer followId;
    private boolean isFollowing;

    // fromFollower 오버로드 (기본)..
    public static FollowUserResponse fromFollower(Follow follow) {
        return fromFollower(follow, false); // 기본값 false
    }

    //  Follow 객체 기반으로 바꾸기..
    public static FollowUserResponse fromFollower(Follow follow, boolean isFollowing) {
        User follower = follow.getFollower(); // 요청 보낸 사람..
        return new FollowUserResponse(
                follower.getId(),
                follower.getEmail(),
                follower.getNickname(),
                follower.getProfileImageUrl(),
                follow.getId(),
                isFollowing
        );
    }

    // fromFollowee 오버로드 (기본)..
    public static FollowUserResponse fromFollowee(Follow follow) {
        return fromFollowee(follow, false); // 기본값 false..
    }

    public static FollowUserResponse fromFollowee(Follow follow, boolean isFollowing) {
        User followee = follow.getFollowee(); // 요청 받은 사람..
        return new FollowUserResponse(
                followee.getId(),
                followee.getEmail(),
                followee.getNickname(),
                followee.getProfileImageUrl(),
                follow.getId(),
                isFollowing
        );
    }





}
