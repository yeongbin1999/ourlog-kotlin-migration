package com.back.ourlog.external.spotify.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExternalUrls(
    val spotify: String? = null
)
