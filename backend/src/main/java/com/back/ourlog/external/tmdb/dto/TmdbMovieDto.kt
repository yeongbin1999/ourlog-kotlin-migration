package com.back.ourlog.external.tmdb.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbMovieDto(
    val id: Int = 0,
    val title: String? = null,

    @JsonProperty("overview")
    val description: String? = null,

    @JsonProperty("poster_path")
    val posterPath: String? = null,

    @JsonProperty("release_date")
    val releaseDate: String? = null,

    @JsonProperty("vote_average")
    val voteAverage: Double = 0.0,

    @JsonProperty("vote_count")
    val voteCount: Int = 0,

    val genres: List<TmdbGenreDto?>? = null
)
