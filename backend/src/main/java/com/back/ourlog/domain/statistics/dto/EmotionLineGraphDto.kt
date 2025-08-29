package com.back.ourlog.domain.statistics.dto

data class EmotionLineGraphDto(
    val axisLabel: String,  // "2025-07" (월) 또는 "2025-07-30" (일)
    val emotion: String,    // 감정명 (tag.name)
    val count: Long         // 일기 개수
)