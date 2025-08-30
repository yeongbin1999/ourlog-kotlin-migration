package com.back.ourlog.external.spotify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 전체 응답에서 불필요한 필드 무시
public class SpotifySearchResponse {
    private Tracks tracks;

    @Getter
    @NoArgsConstructor
    public static class Tracks {
        private List<TrackItem> items;
    }
}

