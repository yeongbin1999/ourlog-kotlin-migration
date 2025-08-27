package com.back.ourlog.domain.comment.dto;

import com.back.ourlog.domain.comment.entity.Comment;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponseDto {
    private int id;
    private Integer userId;
    private String nickname;
    private String profileImageUrl;
    private String content;
    private LocalDateTime createdAt;

    public CommentResponseDto(Comment comment) {
        id = comment.getId();
        userId = comment.getUser() != null ? comment.getUser().getId() : null;
        nickname = comment.getUser() != null ? comment.getUser().getNickname() : null;
        profileImageUrl = comment.getUser() != null ? comment.getUser().getProfileImageUrl() : null;
        content = comment.getContent();
        createdAt = comment.getCreatedAt();
    }
}
