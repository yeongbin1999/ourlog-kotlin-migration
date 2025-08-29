package com.back.ourlog.domain.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TypeGraphResponse {
    private List<TypeLineGraphDto> typeLineGraph;   // 선 차트용 데이터
    private List<TypeRankDto> typeRanking;   // 막대 차트용 데이터
}
