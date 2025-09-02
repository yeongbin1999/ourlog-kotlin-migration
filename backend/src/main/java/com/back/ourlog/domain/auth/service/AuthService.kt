package com.back.ourlog.domain.auth.service

import com.back.ourlog.domain.auth.dto.SignupRequest
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.domain.user.entity.User.Companion.createNormalUser
import com.back.ourlog.domain.user.repository.UserRepository
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import com.back.ourlog.global.security.jwt.TokenDto
import com.back.ourlog.global.security.service.CustomUserDetails
import com.back.ourlog.global.security.service.TokenService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService
) {

    @Transactional
    fun signup(request: SignupRequest) {
        if (userRepository.findByEmail(request.email).isPresent) {
            throw CustomException(ErrorCode.USER_DUPLICATE_EMAIL)
        }

        val user = createNormalUser(
            email = request.email,
            encodedPassword = passwordEncoder.encode(request.password),
            nickname = request.nickname
        )

        userRepository.save(user)
    }

    @Transactional(readOnly = true)
    fun login(email: String, password: String, deviceId: String): TokenDto {
        val user = userRepository.findByEmail(email)
            .orElseThrow { CustomException(ErrorCode.LOGIN_FAILED) }

        if (!passwordEncoder.matches(password, user.password)) {
            throw CustomException(ErrorCode.LOGIN_FAILED)
        }

        return tokenService.issueTokens(CustomUserDetails(user), deviceId)
    }

    fun logout(accessToken: String, deviceId: String) {
        tokenService.logout(extractUserId(accessToken), deviceId)
    }

    fun reissue(refreshToken: String, deviceId: String): TokenDto {
        return tokenService.reIssueTokens(refreshToken, deviceId)
    }

    private fun extractUserId(accessToken: String): Int {
        return tokenService.extractUserId(accessToken)
    }

    @Transactional
    fun findOrCreateOAuthUser(
        providerId: String,
        email: String,
        nickname: String,
        profileImageUrl: String?,
        provider: String
    ): CustomUserDetails {
        val user = userRepository.findByProviderAndProviderId(provider, providerId)
            .orElseGet {
                val newUser = User.createSocialUser(
                    provider = provider,
                    providerId = providerId,
                    email = email,
                    nickname = nickname,
                    profileImageUrl = profileImageUrl
                )
                userRepository.save(newUser)
            }
        return CustomUserDetails(user)
    }
}