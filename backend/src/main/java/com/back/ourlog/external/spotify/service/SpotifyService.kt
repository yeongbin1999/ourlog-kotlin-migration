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
        return try {
            val id = externalId.removePrefix("spotify-")
            val track = spotifyClient.getTrackById(id)
                ?: throw CustomException(ErrorCode.CONTENT_NOT_FOUND)

            val artistId = track.artists?.firstOrNull()?.id
                ?: throw CustomException(ErrorCode.CONTENT_NOT_FOUND)

            val genres = spotifyClient.fetchGenresByArtistId(artistId).filterNotNull()
            toContentSearchResult(track, genres)
        } catch (e: Exception) {
            throw CustomException(ErrorCode.CONTENT_NOT_FOUND)
        }
    }

    fun searchMusicByTitle(title: String): List<ContentSearchResultDto> {
        val response = spotifyClient.searchTrack(title)
        val tracks = response?.tracks?.items ?: emptyList()

        return tracks
            .filterNotNull()
            .filter { track ->
                track.name?.lowercase()?.contains(title.lowercase()) == true
            }
            .sortedByDescending { it.popularity }
            .take(10)
            .mapNotNull { track ->
                val externalId = "spotify-${track.id}"
                try {
                    searchMusicByExternalId(externalId)
                } catch (e: CustomException) {
                    null
                }
            }
    }

    private fun toContentSearchResult(trackItem: TrackItem, genres: List<String>): ContentSearchResultDto {
        val creatorName = trackItem.artists?.firstOrNull()?.name
        val posterUrl = trackItem.album?.images?.firstOrNull()?.url
        val releaseDate = trackItem.album?.releaseDate

        val releasedAt = try {
            releaseDate?.let { LocalDate.parse(it).atStartOfDay() }
        } catch (ignored: Exception) {
            null
        }

        return ContentSearchResultDto(
            externalId = "spotify-${trackItem.id}",
            title = trackItem.name,
            creatorName = creatorName,
            description = null,
            posterUrl = posterUrl,
            releasedAt = releasedAt,
            type = ContentType.MUSIC,
            genres = genres
        )
    }
}
