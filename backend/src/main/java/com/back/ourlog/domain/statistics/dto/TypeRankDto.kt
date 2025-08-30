package com.back.ourlog.domain.statistics.dto

import com.back.ourlog.domain.content.entity.ContentType

data class TypeRankDto(
    val type: ContentType,
    val totalCount: Long
)
