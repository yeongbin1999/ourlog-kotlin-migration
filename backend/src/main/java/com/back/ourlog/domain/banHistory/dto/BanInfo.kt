package com.back.ourlog.domain.banHistory.dto

import com.back.ourlog.domain.banHistory.entity.BanHistory
import java.io.Serializable
import java.time.LocalDateTime

data class BanInfo (
    private var bannedAt: LocalDateTime,
    private var expiredAt: LocalDateTime,
    private var reason: String
) : Serializable {

    val isStillBanned: Boolean
        get() = expiredAt.isAfter(LocalDateTime.now())

    companion object {
        fun from(ban: BanHistory): BanInfo {
            return BanInfo(ban.bannedAt, ban.expiredAt, ban.reason)
        }
    }
}
