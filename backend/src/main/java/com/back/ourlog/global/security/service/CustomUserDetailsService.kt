package com.back.ourlog.global.security.service

import com.back.ourlog.domain.user.service.UserService
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userService: UserService
) : UserDetailsService {

    override fun loadUserByUsername(email: String): CustomUserDetails =
        CustomUserDetails(userService.findByEmail(email))

    fun loadUserById(userId: String): CustomUserDetails =
        CustomUserDetails(userService.findById(userId.toInt()))
}