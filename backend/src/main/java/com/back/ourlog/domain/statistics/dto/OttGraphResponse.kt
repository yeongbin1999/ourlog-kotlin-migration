package com.back.ourlog.domain.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OttGraphResponse {
    private List<OttLineGraphDto> ottLineGraph;
    private List<OttRankDto> ottRanking;
}
