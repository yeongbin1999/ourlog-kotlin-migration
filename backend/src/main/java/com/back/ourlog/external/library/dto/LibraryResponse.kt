package com.back.ourlog.external.library.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class LibraryResponse(
    @JsonProperty("docs")
    val docs: List<LibraryBookDto> = emptyList()
)
