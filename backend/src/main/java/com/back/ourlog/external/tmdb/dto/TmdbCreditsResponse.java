package com.back.ourlog.external.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbCreditsResponse {
    private List<TmdbCrewDto> crew;
}
