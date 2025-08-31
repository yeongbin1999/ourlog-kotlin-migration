package com.back.ourlog.external.spotify.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifySearchResponse(
    val tracks: Tracks? = null
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Tracks(
        val items: List<TrackItem?>? = null
    )
}
