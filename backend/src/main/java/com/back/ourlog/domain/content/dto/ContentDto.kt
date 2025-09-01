package com.back.ourlog.domain.content.dto

import com.back.ourlog.domain.content.entity.Content
import com.back.ourlog.domain.content.entity.ContentType
import java.time.LocalDateTime

data class ContentDto(
    val title: String?,
    val type: ContentType?,
    val creatorName: String?,
    val description: String?,
    val posterUrl: String?,
    val releasedAt: LocalDateTime?,
    val externalId: String?
) {
    companion object {
        fun from(content: Content): ContentDto = ContentDto(
            title = content.title,
            type = content.type,
            creatorName = content.creatorName,
            description = content.description,
            posterUrl = content.posterUrl,
            releasedAt = content.releasedAt,
            externalId = content.externalId
        )
    }
}
