package com.back.ourlog.domain.content.dto;

import com.back.ourlog.domain.content.entity.Content;
import com.back.ourlog.domain.content.entity.ContentType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContentResponseDto {
    private int id;
    private String externalId;
    private ContentType type;
    private String posterUrl;
    private String title;
    private String creatorName;
    private String description;
    private LocalDateTime releasedAt;

    public ContentResponseDto(Content content) {
        this.id = content.getId();
        this.externalId = content.getExternalId();
        this.type = content.getType();
        this.posterUrl = content.getPosterUrl();
        this.title = content.getTitle();
        this.creatorName = content.getCreatorName();
        this.description = content.getDescription();
        this.releasedAt = content.getReleasedAt();
    }
}
