package com.back.ourlog.domain.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FavoriteEmotionAndCountDto {
    private String favoriteEmotion;
    private long favoriteEmotionCount;
}
