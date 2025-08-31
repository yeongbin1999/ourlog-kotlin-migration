package com.back.ourlog.domain.statistics.dto

import com.back.ourlog.domain.statistics.enums.PeriodOption

data class TypeGraphRequest(
    val userId: Int?, // 특정 회원
    val period: PeriodOption // 기간 옵션
)
