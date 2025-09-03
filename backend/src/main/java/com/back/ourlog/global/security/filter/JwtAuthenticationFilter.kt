package com.back.ourlog.global.security.filter

import com.back.ourlog.global.security.jwt.JwtAuthenticationProvider
import com.back.ourlog.global.security.jwt.JwtAuthenticationToken
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter (
    private val authenticationProvider: JwtAuthenticationProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")

        if (header != null && header.startsWith("Bearer ")) {
            val token = header.substring(7)
            val authToken = JwtAuthenticationToken(token)

            // 예외 발생 시 자동으로 AuthenticationEntryPoint로 감
            val authResult = authenticationProvider.authenticate(authToken)
            SecurityContextHolder.getContext().authentication = authResult
        }

        filterChain.doFilter(request, response)
    }
}