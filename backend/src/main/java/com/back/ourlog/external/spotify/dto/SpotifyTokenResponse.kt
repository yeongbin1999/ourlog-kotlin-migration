package com.back.ourlog.external.spotify.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifyTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String? = null,

    @JsonProperty("token_type")
    val tokenType: String? = null,

    @JsonProperty("expires_in")
    val expiresIn: Int = 0
)
