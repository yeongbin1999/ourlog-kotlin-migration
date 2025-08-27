package com.back.ourlog.external.library.dto;

import com.back.ourlog.domain.content.entity.ContentType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LibraryApiResponseDto {
    private String title;
    private ContentType contentType;
    private String creatorName;
    private String description;
    private String posterUrl;
    private LocalDateTime releasedAt;
    private List<String> genres;

    public LibraryApiResponseDto(String title, String creatorName, String description, String posterUrl, LocalDateTime releasedAt, List<String> genres) {
        this.title = title;
        this.contentType = ContentType.BOOK;
        this.creatorName = creatorName;
        this.description = description;
        this.posterUrl = posterUrl;
        this.releasedAt = releasedAt;
        this.genres = genres;
    }
}
