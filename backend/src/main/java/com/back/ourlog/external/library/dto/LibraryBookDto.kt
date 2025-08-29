package com.back.ourlog.external.library.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class LibraryBookDto(
    @JsonProperty("TITLE") val title: String? = null,
    @JsonProperty("AUTHOR") val author: String? = null,
    @JsonProperty("EA_ISBN") val isbn: String? = null,
    @JsonProperty("CONTROL_NO") val controlNo: String? = null,
    @JsonProperty("SUBJECT") val subject: String? = null,
    @JsonProperty("KDC") val kdc: String? = null,
    @JsonProperty("TITLE_URL") val titleUrl: String? = null,
    @JsonProperty("PUBLISH_PREDATE") val publishPredate: String? = null
)
