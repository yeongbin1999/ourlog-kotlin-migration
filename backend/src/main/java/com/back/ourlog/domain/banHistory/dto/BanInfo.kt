package com.back.ourlog.domain.banHistory.dto

import com.back.ourlog.domain.banHistory.entity.BanHistory
import java.time.LocalDateTime

data class BanInfo(
    val bannedAt: LocalDateTime,
    val expiredAt: LocalDateTime?,
    val reason: String
) {
    val isStillBanned: Boolean
        get() = expiredAt?.isAfter(LocalDateTime.now()) ?: true

    companion object {
        fun from(ban: BanHistory): BanInfo {
            return BanInfo(
                bannedAt = ban.bannedAt,
                expiredAt = ban.expiredAt,
                reason = ban.reason
            )
        }
    }
}