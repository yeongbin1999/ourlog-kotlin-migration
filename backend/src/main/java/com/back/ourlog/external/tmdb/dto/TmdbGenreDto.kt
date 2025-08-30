package com.back.ourlog.external.tmdb.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbGenreDto(
    val id: Int = 0,
    val name: String? = null
)
