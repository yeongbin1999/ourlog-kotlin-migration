package com.back.ourlog.domain.user.service

import com.back.ourlog.domain.user.dto.MyProfileResponse
import com.back.ourlog.domain.user.dto.UserProfileResponse
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.domain.user.repository.UserRepository
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun findById(userId: Int): User =
        userRepository.findById(userId)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

    fun findByEmail(email: String): User =
        userRepository.findByEmail(email)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

    fun getMyProfile(userId: Int): MyProfileResponse =
        MyProfileResponse.from(findById(userId))

    fun getUserProfile(userId: Int): UserProfileResponse =
        UserProfileResponse.from(findById(userId))

    fun searchUsersByNickname(keyword: String, pageable: Pageable): Page<UserProfileResponse> =
        userRepository.findByNicknameContainingIgnoreCase(keyword, pageable)
            .map { UserProfileResponse.from(it) }
}