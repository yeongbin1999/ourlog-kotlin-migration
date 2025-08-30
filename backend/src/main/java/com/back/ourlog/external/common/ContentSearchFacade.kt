package com.back.ourlog.external.common

import com.back.ourlog.domain.content.dto.ContentSearchResultDto
import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.external.library.service.LibraryService
import com.back.ourlog.external.spotify.service.SpotifyService
import com.back.ourlog.external.tmdb.service.TmdbService
import com.back.ourlog.global.config.cache.CacheNames
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component

@Component
class ContentSearchFacade(
    private val spotifyService: SpotifyService,
    private val tmdbService: TmdbService,
    private val libraryService: LibraryService,
    private val cacheManager: CacheManager,
) {

    fun search(type: ContentType, externalId: String): ContentSearchResultDto {
        val key = "$type:$externalId"
        val cache = cacheManager.getCache(CacheNames.EXTERNAL_CONTENT)
            ?: throw IllegalStateException("Cache not found: ${CacheNames.EXTERNAL_CONTENT}")

        return cache.get(key, ContentSearchResultDto::class.java) ?: run {
            val result = when (type) {
                ContentType.MUSIC -> spotifyService.searchMusicByExternalId(externalId)
                ContentType.MOVIE -> tmdbService.searchMovieByExternalId(externalId)
                ContentType.BOOK -> libraryService.searchBookByExternalId(externalId)
            } ?: throw CustomException(ErrorCode.CONTENT_NOT_FOUND)

            cache.put(key, result)
            result
        }
    }

    fun searchByTitle(type: ContentType, title: String): List<ContentSearchResultDto> =
        when (type) {
            ContentType.MUSIC -> spotifyService.searchMusicByTitle(title)
            ContentType.MOVIE -> tmdbService.searchMovieByTitle(title)
            ContentType.BOOK  -> libraryService.searchBookByTitle(title)
        }
}
