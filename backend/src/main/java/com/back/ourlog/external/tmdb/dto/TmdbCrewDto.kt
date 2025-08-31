package com.back.ourlog.external.tmdb.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbCrewDto(
    val job: String? = null,
    val name: String? = null
)
