package com.back.ourlog.domain.statistics.dto

data class GenreLineGraphDto(
    val axisLabel: String, // "2025-07" 또는 "2025-07-29"
    val genre: String,
    val count: Long
)
