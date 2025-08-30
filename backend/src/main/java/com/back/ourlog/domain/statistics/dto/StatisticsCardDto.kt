package com.back.ourlog.domain.statistics.dto

data class StatisticsCardDto(
    var totalDiaryCount: Long,
    var averageRating: Double,
    var favoriteTypeAndCountDto: FavoriteTypeAndCountDto,
    var favoriteEmotionAndCountDto: FavoriteEmotionAndCountDto
)
