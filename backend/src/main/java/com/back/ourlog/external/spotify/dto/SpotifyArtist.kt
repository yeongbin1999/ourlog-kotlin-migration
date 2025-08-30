package com.back.ourlog.external.spotify.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifyArtist(
    val id: String? = null,
    val name: String? = null
)
