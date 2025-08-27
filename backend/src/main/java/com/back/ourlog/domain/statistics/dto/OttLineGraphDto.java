package com.back.ourlog.domain.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OttLineGraphDto {
    private String axisLabel;   // "2025-07" 또는 "2025-07-29"
    private String ottName;     // 넷플릭스, 왓챠, 티빙, ...
    private Long count;         // 기록 수
}
