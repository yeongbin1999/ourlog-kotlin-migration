package com.back.ourlog.domain.comment.repository

import com.back.ourlog.domain.comment.entity.Comment
import org.springframework.data.jpa.repository.JpaRepository


interface CommentRepository : JpaRepository<Comment, Int>, CommentRepositoryCustom {
    // 특정 일기에 달린 댓글 수를 계산..
    fun countByDiaryId(diaryId: Int): Int
}