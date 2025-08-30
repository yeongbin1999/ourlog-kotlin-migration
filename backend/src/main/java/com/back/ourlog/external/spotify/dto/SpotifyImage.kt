package com.back.ourlog.external.spotify.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifyImage(
    val url: String? = null
)
