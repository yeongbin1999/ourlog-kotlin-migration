package com.back.ourlog.domain.user.controller;

import com.back.ourlog.domain.user.dto.MyProfileResponse;
import com.back.ourlog.domain.user.dto.UserProfileResponse;
import com.back.ourlog.domain.user.service.UserService;
import com.back.ourlog.global.common.dto.PageResponse;
import com.back.ourlog.global.common.dto.RsData;
import com.back.ourlog.global.security.service.CustomUserDetails;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    @GetMapping("/users/me")
    @PreAuthorize("isAuthenticated()")
    public RsData<MyProfileResponse> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        MyProfileResponse profile = userService.getMyProfile(userDetails.getId());
        return RsData.success("내 프로필 조회 성공", profile);
    }

    @GetMapping("/users/{userId}")
    @PermitAll
    public RsData<UserProfileResponse> getUserProfile(@PathVariable Integer userId) {
        UserProfileResponse profile = userService.getUserProfile(userId);
        return RsData.success("유저 프로필 조회 성공", profile);
    }

    @GetMapping("/users/search")
    @PermitAll
    public RsData<PageResponse<UserProfileResponse>> searchUsers(@RequestParam String keyword, Pageable pageable) {
        Page<UserProfileResponse> results = userService.searchUsersByNickname(keyword, pageable);
        return RsData.success("유저 검색 성공", PageResponse.from(results));
    }
}
