package com.back.ourlog.external.tmdb.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbCreditsResponse(
    val crew: List<TmdbCrewDto?>? = null
)
