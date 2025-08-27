package com.back.ourlog.external.tmdb.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class TmdbSearchResponse {
    private int page;
    private List<TmdbMovieDto> results;
}
