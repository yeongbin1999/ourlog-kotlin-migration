package com.back.ourlog.domain.statistics.repository

import com.back.ourlog.domain.diary.entity.Diary
import com.back.ourlog.domain.statistics.dto.FavoriteEmotionAndCountDto
import com.back.ourlog.domain.statistics.dto.FavoriteTypeAndCountDto
import com.back.ourlog.domain.statistics.dto.TypeCountDto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface StatisticsRepository : JpaRepository<Diary, Int>, StatisticsRepositoryCustom {
}
