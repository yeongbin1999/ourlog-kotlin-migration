package com.back.ourlog.domain.content.dto;

import com.back.ourlog.domain.content.entity.Content;
import com.back.ourlog.domain.content.entity.ContentType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ContentDto {
    private String title;
    private ContentType type;
    private String creatorName;
    private String description;
    private String posterUrl;
    private LocalDateTime releasedAt;
    private String externalId;

    public static ContentDto from(Content content) {
        return ContentDto.builder()
                .title(content.getTitle())
                .creatorName(content.getCreatorName())
                .description(content.getDescription())
                .posterUrl(content.getPosterUrl())
                .releasedAt(content.getReleasedAt())
                .externalId(content.getExternalId())
                .type(content.getType())
                .build();
    }
}
