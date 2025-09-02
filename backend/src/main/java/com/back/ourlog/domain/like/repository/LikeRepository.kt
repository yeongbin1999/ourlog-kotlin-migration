package com.back.ourlog.domain.like.repository

import com.back.ourlog.domain.like.entity.Like
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional

interface LikeRepository : JpaRepository<Like, Long> {
    fun existsByUserIdAndDiaryId(userId: Int, diaryId: Int): Boolean  // 특정 사용자가 일기에 좋아요를 눌렀는지 여부..

    @Transactional
    fun deleteByUserIdAndDiaryId(userId: Int, diaryId: Int)  // 특정 사용자가 일기에 눌렀던 좋아요를 삭제..

    fun countByDiaryId(diaryId: Int): Int // 특정 일기에 달린 좋아요 수를 계산..
}