package com.back.ourlog.external.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbCrewDto {
    private String job;
    private String name;
}
