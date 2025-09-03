package com.back.ourlog.domain.follow.entity

import com.back.ourlog.domain.follow.enums.FollowStatus
import com.back.ourlog.domain.user.entity.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class Follow(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    val follower: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followee_id", nullable = false)
    val followee: User,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0

    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: FollowStatus = FollowStatus.PENDING

    fun accept() {
        this.status = FollowStatus.ACCEPTED
    }

    fun reject() {
        this.status = FollowStatus.REJECTED
    }
}