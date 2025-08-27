package com.back.ourlog.domain.diary.dto;

import com.back.ourlog.domain.content.entity.ContentType;
import jakarta.validation.constraints.*;

import java.util.List;

public record DiaryUpdateRequestDto(
        @NotBlank(message = "제목을 입력해주세요.")
        String title,

        @NotBlank(message = "내용을 입력해주세요.")
        String contentText,

        @NotNull
        Boolean isPublic,

        @NotNull(message = "평점을 입력해주세요.")
        @DecimalMin(value = "0.0", message = "평점은 0 이상이어야 합니다.")
        @DecimalMax(value = "5.0", message = "평점은 5 이하이어야 합니다.")
        Float rating,

        @NotNull
        String externalId,

        @NotNull
        ContentType type,

        @NotEmpty(message = "태그는 하나 이상 선택해야 합니다.")
        List<@NotBlank String> tagNames,

        List<@NotNull Integer> genreIds,
        List<@NotNull Integer> ottIds
) {}
