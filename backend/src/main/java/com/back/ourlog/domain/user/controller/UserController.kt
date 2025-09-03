package com.back.ourlog.domain.user.controller

import com.back.ourlog.domain.user.dto.MyProfileResponse
import com.back.ourlog.domain.user.dto.UserProfileResponse
import com.back.ourlog.domain.user.service.UserService
import com.back.ourlog.global.common.dto.PageResponse
import com.back.ourlog.global.common.dto.RsData
import com.back.ourlog.global.common.extension.toSuccessResponse
import com.back.ourlog.global.security.service.CustomUserDetails
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
class UserController (
    private val userService: UserService
) {

    @GetMapping("/users/me")
    fun getMe(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<RsData<MyProfileResponse>> {
        return userService.getMyProfile(userDetails.id).toSuccessResponse("내 프로필 조회 성공")
    }

    @GetMapping("/users/{userId}")
    fun getUserProfile(@PathVariable userId: Int): ResponseEntity<RsData<UserProfileResponse>> {
        return userService.getUserProfile(userId).toSuccessResponse("유저 프로필 조회 성공")
    }

    @GetMapping("/users/search")
    fun searchUsers(@RequestParam keyword: String,
        pageable: Pageable
    ): ResponseEntity<RsData<PageResponse<UserProfileResponse>>> {
        val pageResult = userService.searchUsersByNickname(keyword, pageable)
        val response = PageResponse.from(pageResult)
        return response.toSuccessResponse("유저 검색 성공")
    }
}
