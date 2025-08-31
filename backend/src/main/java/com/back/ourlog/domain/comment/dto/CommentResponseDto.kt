package com.back.ourlog.domain.comment.dto

import com.back.ourlog.domain.comment.entity.Comment
import java.time.LocalDateTime

data class CommentResponseDto(
    val id: Int,
    val userId: Int,
    val nickname: String,
    val profileImageUrl: String ?,
    val content: String,
    val createdAt: LocalDateTime
) {
    constructor(comment: Comment) : this (
        id = comment.id,
        userId = comment.user.id ?: throw IllegalStateException("Comment user ID cannot be null"),
        nickname = comment.user.nickname,
        profileImageUrl = comment.user.profileImageUrl,
        content = comment.content,
        createdAt = comment.createdAt
    )
}