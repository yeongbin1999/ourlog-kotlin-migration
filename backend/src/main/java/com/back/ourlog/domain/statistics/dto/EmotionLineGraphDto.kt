package com.back.ourlog.domain.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmotionLineGraphDto {
    private String axisLabel; // "2025-07" (월) 또는 "2025-07-30" (일)
    private String emotion;   // 감정명 (tag.name)
    private Long count;       // 일기 개수
}