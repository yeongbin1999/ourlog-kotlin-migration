package com.back.ourlog.domain.user.dto;

import com.back.ourlog.domain.user.entity.User;

public record UserProfileResponse(
        Integer userId,
        String email,
        String nickname,
        String profileImageUrl,
        String bio,
        Integer followingsCount,
        Integer followersCount
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
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