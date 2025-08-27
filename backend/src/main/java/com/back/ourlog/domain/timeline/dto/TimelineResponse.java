package com.back.ourlog.domain.timeline.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 타임라인에 띄울 데이터를 프론트에 전달할 때 사용..
@Getter
@AllArgsConstructor
public class TimelineResponse {
    private Integer id;
    private String title;
    private String content;
    private String createdAt;
    private String imageUrl;
    private int likeCount;
    private int commentCount;

    @JsonProperty("isLiked")
    private boolean isLiked;

    private UserSummary user;

    @Getter
    @AllArgsConstructor
    public static class UserSummary {
        private Integer id;
        private String nickname;
        private String profileImageUrl;
    }
}
