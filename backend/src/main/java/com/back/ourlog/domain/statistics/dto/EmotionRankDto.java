package com.back.ourlog.domain.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmotionRankDto {
    private String emotion;   // 감정명
    private Long totalCount;  // 총 감정 개수
}
