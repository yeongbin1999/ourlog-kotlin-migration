package com.back.ourlog.domain.diary.dto;

import com.back.ourlog.domain.diary.entity.Diary;

import java.time.LocalDateTime;

public record DiaryDto (
        Integer id,
        Integer userId,
        Integer contentId,
        String title,
        String contentText,
        Float rating,
        Boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public DiaryDto(Diary diary) {
        this(
                diary.getId(),
                diary.getUser().getId(),
                diary.getContent().getId(),
                diary.getTitle(),
                diary.getContentText(),
                diary.getRating(),
                diary.getIsPublic(),
                diary.getCreatedAt(),
                diary.getUpdatedAt()
        );
    }
}
