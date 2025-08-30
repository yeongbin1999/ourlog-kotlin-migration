package com.back.ourlog.domain.diary.dto

import com.back.ourlog.domain.diary.entity.Diary
import java.io.Serializable
import java.time.format.DateTimeFormatter

@JvmRecord
data class DiaryResponseDto(
    val id: Int,
    val userId: Int?,
    val contentId: Int?,
    val title: String,
    val contentText: String,
    val rating: Float,
    val isPublic: Boolean,
    val createdAt: String?,
    val modifiedAt: String?,
    val releasedAt: String?,
    val genres: List<String>,
    val tags: List<String>,
    val otts: List<String>
) : Serializable {
    companion object {
        @JvmStatic
        fun from(diary: Diary): DiaryResponseDto {
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            return DiaryResponseDto(
                diary.id,
                diary.user?.id,
                diary.content?.id,
                diary.title,
                diary.contentText,
                diary.rating,
                diary.isPublic,
                diary.createdAt?.toString(),
                diary.updatedAt?.toString(),
                diary.content?.releasedAt?.format(dateFormatter),
                diary.diaryGenres.map { it.genre.name },
                diary.diaryTags.map { it.tag.name },
                diary.diaryOtts.map { it.ott.name }
            )
        }
    }
}
