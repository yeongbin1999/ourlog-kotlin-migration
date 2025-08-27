package com.back.ourlog.domain.statistics.dto;

import com.back.ourlog.domain.statistics.enums.PeriodOption;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TypeGraphRequest {
    private Integer userId;          // 특정 회원
    private PeriodOption period;     // 기간 옵션
}
