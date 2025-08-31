package com.back.ourlog.domain.diary.dto

import com.back.ourlog.domain.diary.entity.Diary
import java.time.LocalDateTime

data class DiaryDto(
    val id: Int,
    val userId: Int,
    val contentId: Int,
    val title: String,
    val contentText: String,
    val rating: Float,
    val isPublic: Boolean,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
    constructor(diary: Diary) : this(
        diary.id,
        diary.user.id,
        diary.content.id,
        diary.title,
        diary.contentText,
        diary.rating,
        diary.isPublic,
        diary.createdAt,
        diary.updatedAt
    )
}
