package com.back.ourlog.domain.content.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.back.ourlog.domain.content.entity.ContentType
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class ContentSearchResultDto(
    @JsonProperty("externalId")
    val externalId: String,

    @JsonProperty("title")
    val title: String? = null,

    @JsonProperty("creatorName")
    val creatorName: String? = null,

    @JsonProperty("description")
    val description: String? = null,

    @JsonProperty("posterUrl")
    val posterUrl: String? = null,

    @JsonProperty("releasedAt")
    val releasedAt: LocalDateTime? = null,

    @JsonProperty("type")
    val type: ContentType,

    @JsonProperty("genres")
    val genres: List<String> = emptyList()
)