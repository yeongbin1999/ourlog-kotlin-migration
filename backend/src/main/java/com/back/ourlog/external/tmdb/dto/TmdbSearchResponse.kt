package com.back.ourlog.external.tmdb.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbSearchResponse(
    val page: Int = 0,
    val results: List<TmdbMovieDto?>? = null
)
