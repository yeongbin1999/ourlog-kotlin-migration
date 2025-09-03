package com.back.ourlog.domain.user.entity

enum class Role(
    val roleName: String,
    val displayName: String
) {
    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN", "관리자");

    // 스프링 시큐리티 GrantedAuthority 반환용 메서드
    fun authority() = roleName
}