package com.back.ourlog.domain.user.repository

import com.back.ourlog.domain.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, Int> {

    fun findByEmail(email: String): Optional<User>

    fun findByProviderAndProviderId(provider: String, providerId: String): Optional<User>

    fun findByNicknameContainingIgnoreCase(keyword: String, pageable: Pageable): Page<User>
}