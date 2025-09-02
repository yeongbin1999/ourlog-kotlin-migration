package com.back.ourlog.domain.diary.repository

import com.back.ourlog.domain.diary.entity.Diary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

interface DiaryRepositoryCustom {
    fun findPageByUserIdWithToOne(userId: Int, pageable: Pageable): Page<Diary>
    fun findPagePublicByUserIdWithToOne(userId: Int, pageable: Pageable): Page<Diary>
    fun findWithAllById(id: Int): Optional<Diary>
}
