package com.back.ourlog.external.spotify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyAlbum {

    @JsonProperty("release_date")
    private String releaseDate;

    private List<SpotifyImage> images;
}
