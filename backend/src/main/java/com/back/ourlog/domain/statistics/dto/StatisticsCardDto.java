package com.back.ourlog.domain.statistics.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StatisticsCardDto {
    private long totalDiaryCount;
    private double averageRating;
    private String favoriteType;
    private long favoriteTypeCount;
    private String favoriteEmotion;
    private long favoriteEmotionCount;
}
