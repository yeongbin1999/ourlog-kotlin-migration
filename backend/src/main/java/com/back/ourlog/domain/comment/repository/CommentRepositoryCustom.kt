package com.back.ourlog.domain.comment.repository

import com.back.ourlog.domain.comment.entity.Comment
import com.back.ourlog.domain.diary.entity.Diary

interface CommentRepositoryCustom {
    fun findQByDiaryOrderByCreatedAtDesc(diary: Diary): List<Comment>
}