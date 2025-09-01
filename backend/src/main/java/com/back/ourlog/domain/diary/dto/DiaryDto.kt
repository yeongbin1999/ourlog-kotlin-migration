package com.back.ourlog.domain.diary.dto

import com.back.ourlog.domain.diary.entity.Diary
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
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
        id = diary.id,
        userId = (diary.user ?: throw CustomException(ErrorCode.USER_NOT_FOUND)).id,
        contentId = diary.content.id,
        title = diary.title,
        contentText = diary.contentText,
        rating = diary.rating,
        isPublic = diary.isPublic,
        createdAt = diary.createdAt,
        modifiedAt = diary.updatedAt
    )
}
