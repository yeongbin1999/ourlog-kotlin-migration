package com.back.ourlog.external.spotify.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TrackItem(
    val id: String? = null,
    val name: String? = null,
    val artists: List<SpotifyArtist?>? = null,
    val album: SpotifyAlbum? = null,
    val popularity: Int = 0,

    @JsonProperty("external_urls")
    val externalUrls: ExternalUrls? = null
)
