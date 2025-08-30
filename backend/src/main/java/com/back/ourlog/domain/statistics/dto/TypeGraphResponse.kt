package com.back.ourlog.domain.statistics.dto

data class TypeGraphResponse(
    val typeLineGraph: List<TypeLineGraphDto>, // 선 차트용 데이터
    val typeRanking: List<TypeRankDto> // 막대 차트용 데이터
)
