package com.back.ourlog.domain.comment.repository

import com.back.ourlog.domain.comment.entity.Comment

interface CommentRepositoryCustom {
    fun findQByDiaryIdOrderByCreatedAtDesc(diaryId: Int): List<Comment>
}