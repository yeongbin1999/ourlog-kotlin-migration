package com.back.ourlog.domain.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GenreGraphResponse {
    private List<GenreLineGraphDto> genreLineGraph;
    private List<GenreRankDto> genreRanking;
}
