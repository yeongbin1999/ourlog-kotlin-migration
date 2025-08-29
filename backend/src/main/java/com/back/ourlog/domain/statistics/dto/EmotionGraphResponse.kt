package com.back.ourlog.domain.statistics.dto

data class EmotionGraphResponse(
    val emotionLineGraph: List<EmotionLineGraphDto>, // 선 차트용 데이터
    val emotionRanking: List<EmotionRankDto>         // 순위 차트용 데이터
)