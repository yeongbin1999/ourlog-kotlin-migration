package com.back.ourlog.global.security.jwt

import com.back.ourlog.global.exception.ErrorCode
import com.back.ourlog.global.security.exception.JwtAuthenticationException
import com.back.ourlog.global.security.service.CustomUserDetails
import com.back.ourlog.global.security.service.CustomUserDetailsService
import lombok.RequiredArgsConstructor
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
@RequiredArgsConstructor
class JwtAuthenticationProvider (
    private val jwtTokenProvider: JwtProvider,
    private val customUserDetailsService: CustomUserDetailsService
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val token = authentication.credentials as String

        if (!jwtTokenProvider.validateToken(token)) {
            throw JwtAuthenticationException(ErrorCode.AUTH_INVALID_TOKEN)
        }

        val userId = jwtTokenProvider.getUserIdFromToken(token)
        val userDetails: CustomUserDetails = customUserDetailsService.loadUserById(userId)

        return JwtAuthenticationToken(userDetails, userDetails.authorities)
    }

    override fun supports(authentication: Class<*>): Boolean {
        return JwtAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
