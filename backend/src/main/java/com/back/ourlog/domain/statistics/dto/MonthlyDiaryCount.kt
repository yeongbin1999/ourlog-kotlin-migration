package com.back.ourlog.domain.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MonthlyDiaryCount {
    private String period;
    private Long views;
}
