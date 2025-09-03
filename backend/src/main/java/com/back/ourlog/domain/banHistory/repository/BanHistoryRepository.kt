package com.back.ourlog.domain.banHistory.repository

import com.back.ourlog.domain.banHistory.entity.BanHistory
import com.back.ourlog.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BanHistoryRepository : JpaRepository<BanHistory, Int> {
    fun findTopByUserOrderByBannedAtDesc(user: User): BanHistory?

    @Query("SELECT b FROM BanHistory b WHERE b.user = :user AND (b.expiredAt IS NULL OR b.expiredAt > CURRENT_TIMESTAMP)")
    fun findActiveBanByUser(@Param("user") user: User): BanHistory?
}