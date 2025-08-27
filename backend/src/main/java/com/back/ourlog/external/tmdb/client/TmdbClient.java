package com.back.ourlog.external.tmdb.client;

import com.back.ourlog.external.tmdb.dto.TmdbCreditsResponse;
import com.back.ourlog.external.tmdb.dto.TmdbMovieDto;
import com.back.ourlog.external.tmdb.dto.TmdbSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbClient {

    private final RestTemplate restTemplate;

    @Value("${tmdb.api.key}")
    private String apiKey;

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3";

    public TmdbSearchResponse searchMovie(String query) {
        String encodedQuery = UriUtils.encode(query, StandardCharsets.UTF_8);

        // 한글 + 한국 지역 기준 검색
        String url = TMDB_BASE_URL + "/search/movie?api_key=" + apiKey
                + "&query=" + encodedQuery
                + "&language=ko-KR"
                + "&region=KR"
                + "&include_adult=false";

        TmdbSearchResponse response = restTemplate.getForObject(url, TmdbSearchResponse.class);

        // 결과 없을 경우 fallback = 영어로 다시 검색
        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            String fallbackUrl = TMDB_BASE_URL + "/search/movie?api_key=" + apiKey
                    + "&query=" + encodedQuery
                    + "&language=en-US"
                    + "&region=KR"
                    + "&include_adult=false";

            response = restTemplate.getForObject(fallbackUrl, TmdbSearchResponse.class);
        }

        return response;
    }

    public TmdbCreditsResponse fetchCredits(int movieId) {
        String url = TMDB_BASE_URL + "/movie/" + movieId + "/credits?api_key=" + apiKey
                + "&language=ko-KR";

        return restTemplate.getForObject(url, TmdbCreditsResponse.class);
    }

    public TmdbMovieDto fetchMovieById(String id) {
        String url = "https://api.themoviedb.org/3/movie/" + id + "?language=ko-KR&api_key=" + apiKey;
        try {
            ResponseEntity<TmdbMovieDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    TmdbMovieDto.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw e;
        }
    }

}