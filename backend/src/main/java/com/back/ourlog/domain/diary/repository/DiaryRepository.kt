package com.back.ourlog.domain.diary.repository

import com.back.ourlog.domain.diary.entity.Diary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface DiaryRepository : JpaRepository<Diary, Int> {
    fun findTopByOrderByIdDesc(): Diary?
    fun findByUserId(userId: Int, pageable: Pageable): Page<Diary>
    fun findByUserIdAndIsPublicTrue(userId: Int, pageable: Pageable): Page<Diary>
}
