package com.back.ourlog.global.security.jwt

import com.back.ourlog.global.security.service.CustomUserDetails
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class JwtAuthenticationToken : AbstractAuthenticationToken {

    val token: String?
    val principal: CustomUserDetails?

    // 인증 전: 토큰만 있는 상태
    constructor(token: String) : super(null) {
        this.token = token
        this.principal = null
        isAuthenticated = false
    }

    // 인증 완료: principal과 권한 있는 상태
    constructor(principal: CustomUserDetails, authorities: Collection<GrantedAuthority>) : super(authorities) {
        this.token = null
        this.principal = principal
        isAuthenticated = true
    }

    override fun getCredentials(): Any? = token
    override fun getPrincipal(): Any? = principal
}