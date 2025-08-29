package com.back.ourlog.domain.content.dto;

import com.back.ourlog.domain.content.entity.ContentType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
@Builder
public record ContentSearchResultDto(
        String externalId,
        String title,
        String creatorName,
        String description,
        String posterUrl,
        LocalDateTime releasedAt,
        ContentType type,
        List<String> genres
) implements Serializable {

    @JsonCreator
    public ContentSearchResultDto(
            @JsonProperty("externalId") String externalId,
            @JsonProperty("title") String title,
            @JsonProperty("creatorName") String creatorName,
            @JsonProperty("description") String description,
            @JsonProperty("posterUrl") String posterUrl,
            @JsonProperty("releasedAt") LocalDateTime releasedAt,
            @JsonProperty("type") ContentType type,
            @JsonProperty("genres") List<String> genres
    ) {
        this.externalId = externalId;
        this.title = title;
        this.creatorName = creatorName;
        this.description = description;
        this.posterUrl = posterUrl;
        this.releasedAt = releasedAt;
        this.type = type;
        this.genres = genres;
    }
}
