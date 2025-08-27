package com.back.ourlog.external.spotify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackItem {
    private String id;
    private String name;
    private List<SpotifyArtist> artists;
    private SpotifyAlbum album;
    private int popularity;

    @JsonProperty("external_urls")
    private ExternalUrls externalUrls;
}
