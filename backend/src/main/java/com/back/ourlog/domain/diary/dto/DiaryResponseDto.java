package com.back.ourlog.domain.diary.dto;

import com.back.ourlog.domain.diary.entity.Diary;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record DiaryResponseDto(
        Integer id,
        Integer userId,
        Integer contentId,
        String title,
        String contentText,
        Float rating,
        Boolean isPublic,
        String createdAt,
        String modifiedAt,
        String releasedAt,
        List<String> genres,
        List<String> tags,
        List<String> otts
) implements Serializable {

    public static DiaryResponseDto from(Diary diary) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return new DiaryResponseDto(
                diary.getId(),
                diary.getUser() != null ? diary.getUser().getId() : null,
                diary.getContent() != null ? diary.getContent().getId() : null,
                diary.getTitle(),
                diary.getContentText(),
                diary.getRating(),
                diary.getIsPublic(),
                diary.getCreatedAt() != null ? diary.getCreatedAt().toString() : null,
                diary.getUpdatedAt() != null ? diary.getUpdatedAt().toString() : null,
                diary.getContent() != null && diary.getContent().getReleasedAt() != null
                        ? diary.getContent().getReleasedAt().format(dateFormatter)
                        : null,
                diary.getDiaryGenres().stream()
                        .map(dg -> dg.getGenre().getName())
                        .toList(),
                diary.getDiaryTags().stream()
                        .map(dt -> dt.getTag().getName())
                        .toList(),
                diary.getDiaryOtts().stream()
                        .map(do_ -> do_.getOtt().getName())
                        .toList()
        );
    }
}
