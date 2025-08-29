package com.back.ourlog.domain.statistics.dto

data class GenreGraphResponse(
    val genreLineGraph: List<GenreLineGraphDto>,
    val genreRanking: List<GenreRankDto>
)
