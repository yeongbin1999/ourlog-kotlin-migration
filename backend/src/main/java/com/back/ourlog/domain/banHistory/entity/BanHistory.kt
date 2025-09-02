package com.back.ourlog.domain.banHistory.entity

import com.back.ourlog.domain.user.entity.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class BanHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val banType: BanType,

    @Column(length = 255)
    val reason: String = "No reason provided",

    @Column(nullable = false, updatable = false)
    @CreatedDate
    val bannedAt: LocalDateTime = LocalDateTime.now(),

    val expiredAt: LocalDateTime? = null
) {
    val isActiveNow: Boolean
        get() = banType == BanType.PERMANENT || (expiredAt?.isAfter(LocalDateTime.now()) == true)
}