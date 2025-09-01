package com.back.ourlog.domain.diary.dto

import com.back.ourlog.domain.diary.entity.Diary
import java.io.Serializable
import java.time.format.DateTimeFormatter

data class DiaryResponseDto(
    val id: Int,
    val userId: Int,
    val contentId: Int,
    val title: String,
    val contentText: String,
    val rating: Float,
    val isPublic: Boolean,
    val createdAt: String,
    val modifiedAt: String,
    val releasedAt: String?,
    val genres: List<String>,
    val tags: List<String>,
    val otts: List<String>
) : Serializable {
    companion object {
        private val DATE_ONLY = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        fun from(diary: Diary): DiaryResponseDto {
            return DiaryResponseDto(
                id = diary.id,
                userId = diary.user.id,
                contentId = diary.content.id,
                title = diary.title,
                contentText = diary.contentText,
                rating = diary.rating,
                isPublic = diary.isPublic,
                createdAt = diary.createdAt.toString(),
                modifiedAt = diary.updatedAt.toString(),
                releasedAt = diary.content.releasedAt?.format(DATE_ONLY),
                genres = diary.diaryGenres.map { it.genre.name },
                tags = diary.diaryTags.map { it.tag.name },
                otts = diary.diaryOtts.map { it.ott.name }
            )
        }
    }
}
