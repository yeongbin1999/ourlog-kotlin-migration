package com.back.ourlog.domain.statistics.dto

data class OttLineGraphDto(
    val axisLabel: String, // "2025-07" 또는 "2025-07-29"
    val ottName: String, // 넷플릭스, 왓챠, 티빙, ...
    val count: Long // 기록 수
)
