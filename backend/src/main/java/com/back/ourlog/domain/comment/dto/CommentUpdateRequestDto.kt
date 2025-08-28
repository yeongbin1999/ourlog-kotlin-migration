package com.back.ourlog.domain.comment.dto

data class CommentUpdateRequestDto(
    val id: Int = 0,
    val content: String = ""
)