package com.back.ourlog.domain.user.service

import com.back.ourlog.domain.user.dto.MyProfileResponse
import com.back.ourlog.domain.user.dto.UserProfileResponse
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.domain.user.repository.UserRepository
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import com.back.ourlog.global.security.oauth.OAuthAttributes
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

    fun registerOrGetOAuthUser(attributes: OAuthAttributes): User =
        userRepository.findByProviderAndProviderId(attributes.provider, attributes.providerId)
            .orElseGet { registerOAuthUser(attributes) }

    private fun registerOAuthUser(attributes: OAuthAttributes): User {
        val profileImageUrl = attributes.attributes["profile_image"]?.toString()
        val user = User.createSocialUser(
            provider = attributes.provider,
            providerId = attributes.providerId,
            email = attributes.email,
            nickname = attributes.name,
            profileImageUrl = profileImageUrl
        )
        return userRepository.save(user)
    }

    fun getMyProfile(userId: Int): MyProfileResponse =
        MyProfileResponse.from(findById(userId))

    fun getUserProfile(userId: Int): UserProfileResponse =
        UserProfileResponse.from(findById(userId))

    fun searchUsersByNickname(keyword: String, pageable: Pageable): Page<UserProfileResponse> =
        userRepository.findByNicknameContainingIgnoreCase(keyword, pageable)
            .map { UserProfileResponse.from(it) }
}