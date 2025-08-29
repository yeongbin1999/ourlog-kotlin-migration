package com.back.ourlog.domain.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EmotionGraphResponse {
    private List<EmotionLineGraphDto> emotionLineGraph; // 선 차트용 데이터
    private List<EmotionRankDto> emotionRanking;  // 순위 차트용 데이터
}
