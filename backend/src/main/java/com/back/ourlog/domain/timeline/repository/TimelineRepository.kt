package com.back.ourlog.domain.timeline.repository

import com.back.ourlog.domain.diary.entity.Diary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TimelineRepository : JpaRepository<Diary, Int> {

    // 공개된 일기만 최신순 가져오기..
    @Query("SELECT d FROM Diary d WHERE d.isPublic = true ORDER BY d.createdAt DESC")
    fun findPublicDiaries(): List<Diary>
}