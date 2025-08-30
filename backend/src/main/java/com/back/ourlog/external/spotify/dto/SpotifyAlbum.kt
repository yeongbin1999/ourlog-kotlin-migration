package com.back.ourlog.external.spotify.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifyAlbum(
    @JsonProperty("release_date")
    val releaseDate: String? = null,
    val images: List<SpotifyImage?>? = null
)
