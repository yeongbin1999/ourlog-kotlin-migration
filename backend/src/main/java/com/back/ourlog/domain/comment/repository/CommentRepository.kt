package com.back.ourlog.domain.comment.repository

import com.back.ourlog.domain.comment.entity.Comment
import com.back.ourlog.domain.diary.entity.Diary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param


interface CommentRepository : JpaRepository<Comment, Int>, CommentRepositoryCustom {
    // 특정 일기에 달린 댓글 수를 계산..
    fun countByDiaryId(diaryId: Int): Int

    // 댓글정보 - 최신 순 정렬 (최신 댓글 일수록 위에 배치)
    @Query("select c from Comment c where c.diary = :diary order by c.createdAt DESC")
    fun findByDiaryOrderByCreatedAtDesc(@Param("diary") diary: Diary): List<Comment>
}