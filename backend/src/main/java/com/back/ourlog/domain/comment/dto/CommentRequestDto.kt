package com.back.ourlog.domain.comment.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CommentRequestDto(
    @field:NotNull
    val diaryId: Int,

    @field:NotBlank
    val content: String
)