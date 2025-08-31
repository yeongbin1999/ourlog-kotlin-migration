package com.back.ourlog.domain.statistics.dto

data class OttGraphResponse(
    val ottLineGraph: List<OttLineGraphDto>,
    val ottRanking: List<OttRankDto>
)
