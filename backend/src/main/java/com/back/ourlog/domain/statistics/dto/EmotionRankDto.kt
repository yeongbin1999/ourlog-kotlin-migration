package com.back.ourlog.domain.statistics.dto

data class EmotionRankDto(
    val emotion: String,    // 감정명
    val totalCount: Long    // 총 감정 개수
)
