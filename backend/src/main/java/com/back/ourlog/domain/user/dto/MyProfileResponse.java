package com.back.ourlog.domain.user.dto;

import com.back.ourlog.domain.user.entity.User;

public record MyProfileResponse(
        Integer userId,
        String email,
        String nickname,
        String profileImageUrl,
        String bio,
        Integer followingsCount,
        Integer followersCount
) {
    public static MyProfileResponse from(User user) {
        return new MyProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getBio(),
                user.getFollowingsCount(),
                user.getFollowersCount()
        );
    }
}