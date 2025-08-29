package com.back.ourlog.domain.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GenreLineGraphDto {
    private String axisLabel; // "2025-07" 또는 "2025-07-29"
    private String genre;
    private Long count;
}
