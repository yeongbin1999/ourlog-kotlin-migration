package com.back.ourlog.external.spotify.client;

import com.back.ourlog.external.spotify.dto.SpotifySearchResponse;
import com.back.ourlog.external.spotify.dto.SpotifyTokenResponse;
import com.back.ourlog.external.spotify.dto.TrackItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpotifyClient {

    private final RestTemplate restTemplate;

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    private String accessToken;
    private LocalDateTime tokenExpireAt;

    @PostConstruct
    public void init() {
        updateAccessToken(); // 초기 토큰 발급 및 만료 시각 설정
    }

    public SpotifySearchResponse searchTrack(String keyword) {
        // 만료 시 토큰 갱신
        if (tokenExpireAt == null || LocalDateTime.now().isAfter(tokenExpireAt)) {
            log.info("Spotify accessToken 만료됨. 선제 갱신.");
            updateAccessToken();
        }

        try {
            SpotifySearchResponse response = requestSpotify(keyword);
            return response;
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Unauthorized 발생. 강제 갱신 후 재시도.");
            updateAccessToken();
            return requestSpotify(keyword);
        } catch (Exception e) {
            log.error("Spotify 응답 파싱 실패", e);
            throw new RuntimeException("Spotify API 응답 파싱 실패");
        }
    }

    private String getAccessToken() {
        // 토큰이 없거나 만료되었으면 갱신
        if (accessToken == null || tokenExpireAt == null || LocalDateTime.now().isAfter(tokenExpireAt)) {
            updateAccessToken();
        }
        return accessToken;
    }

    private void updateAccessToken() {
        SpotifyTokenResponse response = fetchAccessToken();
        this.accessToken = response.getAccessToken();

        // 토큰 만료 시간 저장 (10초 여유)
        this.tokenExpireAt = LocalDateTime.now().plusSeconds(response.getExpiresIn() - 10);

        log.info("Spotify accessToken 갱신 성공, 만료 시각: {}", tokenExpireAt);
    }

    private SpotifyTokenResponse fetchAccessToken() {
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedCredentials);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<SpotifyTokenResponse> response = restTemplate.postForEntity(
                    "https://accounts.spotify.com/api/token",
                    request,
                    SpotifyTokenResponse.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Spotify access token 요청 실패", e);
            throw new RuntimeException("Spotify API 연동 실패");
        }
    }

    private SpotifySearchResponse requestSpotify(String keyword) {
        String url = "https://api.spotify.com/v1/search?q=" + UriUtils.encode(keyword, StandardCharsets.UTF_8)
                + "&type=track&limit=5";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<SpotifySearchResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                SpotifySearchResponse.class
        );

        return response.getBody();
    }

    public List<String> fetchGenresByArtistId(String artistId) {
        String url = "https://api.spotify.com/v1/artists/" + artistId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            List<String> genres = (List<String>) response.getBody().get("genres");
            return genres != null ? genres : List.of();

        } catch (HttpClientErrorException.Unauthorized e) {
            updateAccessToken();

            // 재시도
            headers.setBearerAuth(getAccessToken());
            request = new HttpEntity<>(headers);

            ResponseEntity<Map> retry = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            List<String> genres = (List<String>) retry.getBody().get("genres");
            return genres != null ? genres : List.of();

        } catch (Exception e) {
            throw new RuntimeException("Spotify 아티스트 장르 조회 중 오류 발생", e);
        }
    }

    public TrackItem getTrackById(String id) {
        String url = "https://api.spotify.com/v1/tracks/%s".formatted(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class
            );

            Map<String, Object> body = response.getBody();

            // 필요한 필드만 파싱해서 TrackItem 객체로 변환
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(body, TrackItem.class);

        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Spotify 트랙 조회 중 오류 발생", e);
        }
    }

}
