package com.back.ourlog.domain.statistics.dto

import com.back.ourlog.domain.content.entity.ContentType

data class TypeLineGraphDto(
    val axisLabel: String, // “2025-03” 또는 “2025-03-14”
    val type: ContentType, // DRAMA, MOVIE, BOOK, MUSIC …
    val count: Long // 일기 수
)
