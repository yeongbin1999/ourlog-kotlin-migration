package com.back.ourlog.domain.comment.dto;

import lombok.Data;

@Data
public class CommentUpdateRequestDto {
    private int id;
    private String content;

    public CommentUpdateRequestDto() {}

    public CommentUpdateRequestDto(int id, String content) {
        this.id = id;
        this.content = content;
    }
}
