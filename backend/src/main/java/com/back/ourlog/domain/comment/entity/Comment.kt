package com.back.ourlog.domain.comment.entity

import com.back.ourlog.domain.diary.entity.Diary
import com.back.ourlog.domain.user.entity.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class Comment (
    @field:JoinColumn(
        name = "diary_id",
        nullable = false
    ) @field:ManyToOne(fetch = FetchType.LAZY)
    var diary: Diary,

    @field:JoinColumn(
        name = "user_id",
        nullable = true
    ) @field:ManyToOne(fetch = FetchType.LAZY)
    var user: User,

    var content: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0

    @CreatedDate
    @Column(updatable = false)
    lateinit var createdAt: LocalDateTime

    @LastModifiedDate
    lateinit var updatedAt: LocalDateTime

    fun update(content: String) {
        this.content = content
    }
}
