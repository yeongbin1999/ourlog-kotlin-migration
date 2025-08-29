package com.back.ourlog.external.common;

import com.back.ourlog.domain.content.dto.ContentSearchResultDto;
import com.back.ourlog.domain.content.entity.Content;
import com.back.ourlog.domain.content.entity.ContentType;
import com.back.ourlog.external.library.service.LibraryService;
import com.back.ourlog.external.spotify.service.SpotifyService;
import com.back.ourlog.external.tmdb.service.TmdbService;
import com.back.ourlog.global.exception.CustomException;
import com.back.ourlog.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ContentSearchFacade {

    private final SpotifyService spotifyService;
    private final TmdbService tmdbService;
    private final LibraryService libraryService;
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    public ContentSearchResultDto search(ContentType type, String externalId) {
        String key = type + ":" + externalId;
        Cache cache = cacheManager.getCache("externalContent");

        if (cache != null) {
            Object rawValue = cache.get(key, Object.class);
            if (rawValue != null) {
                try {
                    return objectMapper.convertValue(rawValue, ContentSearchResultDto.class);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("캐시 역직렬화 실패", e);
                }
            }
        }

        // 캐시가 없거나 실패했을 경우 외부 API 호출
        ContentSearchResultDto result;
        try {
            result = switch (type) {
                case MUSIC -> spotifyService.searchMusicByExternalId(externalId);
                case MOVIE -> tmdbService.searchMovieByExternalId(externalId);
                case BOOK -> libraryService.searchBookByExternalId(externalId);
            };
        } catch (Exception e) {
            throw new RuntimeException("externalId로 콘텐츠 검색 중 오류 발생", e);
        }

        // 캐시에 저장
        if (cache != null && result != null) {
            cache.put(key, result);
        }

        return result;
    }

    public List<ContentSearchResultDto> searchByTitle(ContentType type, String title) {
        try {
            return switch (type) {
                case MUSIC -> spotifyService.searchMusicByTitle(title);
                case MOVIE -> tmdbService.searchMovieByTitle(title);
                case BOOK -> libraryService.searchBookByTitle(title);
            };
        } catch (Exception e) {
            throw new CustomException(ErrorCode.CONTENT_NOT_FOUND);
        }
    }

}
