package com.back.ourlog.external.spotify.service

import com.back.ourlog.domain.content.dto.ContentSearchResultDto
import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.external.spotify.client.SpotifyClient
import com.back.ourlog.external.spotify.dto.TrackItem
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SpotifyService(
    private val spotifyClient: SpotifyClient
) {

    private val log = LoggerFactory.getLogger(SpotifyService::class.java)

    fun searchMusicByExternalId(externalId: String): ContentSearchResultDto {
        try {
            val id = externalId.removePrefix("spotify-")
            val track = spotifyClient.getTrackById(id)
                ?: throw CustomException(ErrorCode.CONTENT_NOT_FOUND)

            val artistId = track.artists?.firstOrNull()?.id
                ?: throw CustomException(ErrorCode.CONTENT_NOT_FOUND)

            val artistInfo = spotifyClient.getSeveralArtists(listOf(artistId))
            val genres = artistInfo?.artists?.firstOrNull()?.genres?.filterNotNull() ?: emptyList()

            return toContentSearchResult(track, genres)
        } catch (e: CustomException) {
            throw e
        } catch (e: Exception) {
            log.error("Failed to search music by external ID: {}", externalId, e)
            throw CustomException(ErrorCode.EXTERNAL_API_ERROR)
        }
    }

    fun searchMusicByTitle(title: String): List<ContentSearchResultDto> {
        val response = spotifyClient.searchTrack(title)
        val tracks = response?.tracks?.items?.filterNotNull() ?: return emptyList()

        val validTracks = tracks
            .filter { it.name?.lowercase()?.contains(title.lowercase()) == true && it.artists?.firstOrNull()?.id != null }
            .sortedByDescending { it.popularity }
            .take(10)

        if (validTracks.isEmpty()) return emptyList()

        val artistIds = validTracks.mapNotNull { it.artists?.firstOrNull()?.id }.distinct()

        // 단 한 번의 API 호출로 모든 아티스트의 장르 정보를 가져옴
        val artistsResponse = spotifyClient.getSeveralArtists(artistIds)

        val artistGenresMap = artistsResponse?.artists?.filterNotNull()?.associate {
            it.id!! to (it.genres?.filterNotNull() ?: emptyList())
        } ?: emptyMap()

        return validTracks.map { track ->
            val artistId = track.artists?.firstOrNull()?.id
            val genres = artistGenresMap[artistId] ?: emptyList()
            toContentSearchResult(track, genres)
        }
    }

    private fun toContentSearchResult(trackItem: TrackItem, genres: List<String>): ContentSearchResultDto {
        val releasedAt = trackItem.album?.releaseDate?.let {
            runCatching { LocalDate.parse(it).atStartOfDay() }.getOrNull()
        }

        return ContentSearchResultDto(
            externalId = "spotify-${trackItem.id}",
            title = trackItem.name,
            creatorName = trackItem.artists?.firstOrNull()?.name,
            description = null,
            posterUrl = trackItem.album?.images?.firstOrNull()?.url,
            releasedAt = releasedAt,
            type = ContentType.MUSIC,
            genres = genres
        )
    }
}
