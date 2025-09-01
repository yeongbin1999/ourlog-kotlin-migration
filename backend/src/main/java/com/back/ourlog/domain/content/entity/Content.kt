package com.back.ourlog.domain.content.entity

import com.back.ourlog.domain.content.dto.ContentSearchResultDto
import com.back.ourlog.domain.diary.entity.Diary
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class Content(
    var title: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: ContentType,

    @Column(name = "creator_name")
    var creatorName: String?,

    @Column(length = 1000)
    var description: String?,

    var posterUrl: String?,

    var releasedAt: LocalDateTime?,

    var externalId: String?
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0

    @CreatedDate
    @Column(updatable = false)
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    var updatedAt: LocalDateTime? = null

    @OneToMany(mappedBy = "content", cascade = [CascadeType.ALL], orphanRemoval = true)
    val diaries: MutableList<Diary> = mutableListOf()

    fun update(externalId: String?, type: ContentType) {
        this.externalId = externalId
        this.type = type
    }

    companion object {
        fun of(result: ContentSearchResultDto): Content {
            val description = if (result.type == ContentType.MOVIE) result.description else null

            return Content(
                title = result.title,
                type = result.type,
                creatorName = result.creatorName,
                description = description,
                posterUrl = result.posterUrl,
                releasedAt = result.releasedAt,
                externalId = result.externalId
            )
        }
    }
}
