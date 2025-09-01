package com.back.ourlog.domain.content.dto

import com.back.ourlog.domain.content.entity.Content
import com.back.ourlog.domain.content.entity.ContentType
import java.time.LocalDateTime

data class ContentResponseDto(
    val id: Int,
    val externalId: String?,
    val type: ContentType?,
    val posterUrl: String?,
    val title: String?,
    val creatorName: String?,
    val description: String?,
    val releasedAt: LocalDateTime?
) {
    companion object {
        fun from(content: Content): ContentResponseDto = ContentResponseDto(
            id = content.id ?: -1,
            externalId = content.externalId,
            type = content.type,
            posterUrl = content.posterUrl,
            title = content.title,
            creatorName = content.creatorName,
            description = content.description,
            releasedAt = content.releasedAt
        )
    }
}
