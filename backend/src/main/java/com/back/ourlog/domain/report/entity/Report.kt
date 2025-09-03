package com.back.ourlog.domain.report.entity

import com.back.ourlog.domain.user.entity.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["reporter_id", "target_id", "type"])
    ]
)
class Report(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: ReportReason,

    @Column(length = 255, nullable = false)
    val description: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    val reporter: User,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_id", nullable = false)
    val target: User,

    @CreatedDate
    @Column(updatable = false, nullable = false)
    val reportedAt: LocalDateTime = LocalDateTime.now()
)

