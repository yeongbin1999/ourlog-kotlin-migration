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
            throw CustomException(ErrorCode.EXTERNAL_API_ERROR)
        }
    }

    fun searchMusicByTitle(title: String): List<ContentSearchResultDto> {
        val response = spotifyClient.searchTrack(title)
        val tracks = response?.tracks?.items?.filterNotNull() ?: emptyList()

        // 초기 검색 결과의 트랙 필터링 + 정렬
        return tracks
            .filter { it.name?.lowercase()?.contains(title.lowercase()) == true }
            .sortedByDescending { it.popularity }
            .take(10)
            .map { track ->
                // 각 트랙에 대한 아티스트 ID
                val artistId = track.artists?.firstOrNull()?.id

                // 아티스트 ID가 있을 경우에만 장르 조회
                val genres = if (artistId != null) {
                    try {
                        spotifyClient.fetchGenresByArtistId(artistId).filterNotNull()
                    } catch (e: Exception) {
                        log.error("아티스트 {}의 장르 조회 실패", artistId, e)
                        emptyList() // 특정 아티스트 조회 실패 시에도 다음 로직이 진행되도록 처리
                    }
                } else {
                    emptyList()
                }

                // 이미 가지고 있는 track 정보와 방금 조회한 genres로 DTO 직접 생성
                toContentSearchResult(track, genres)
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
