package com.back.ourlog.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequestDto {
    private int diaryId;
    @NotBlank
    private String content;
}
