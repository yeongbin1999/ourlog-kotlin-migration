package com.back.ourlog.external.library.service

import com.back.ourlog.domain.content.dto.ContentSearchResultDto
import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.external.library.dto.LibraryBookDto
import com.back.ourlog.external.library.dto.LibraryResponse
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class LibraryService(
    private val libraryWebClient: WebClient,

    @Value("\${library.api-key}")
    private val libraryApiKey: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private val ISBN_10_PATTERN = "\\d{10}".toRegex()
        private val ISBN_13_PATTERN = "\\d{13}".toRegex()
        private const val DATE_PATTERN = "yyyyMMdd"

        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
        private val AUTHOR_PREFIX_REGEX = "(원작자|저자|지은이|글|그림|편저|엮은이)\\s*[:：]\\s*".toRegex()
        private val AUTHOR_SUFFIX_REGEX = "\\s*;\\s*$".toRegex()
        private val GENRE_SEPARATOR_REGEX = "[,:;]".toRegex()
        private val SINGLE_DIGIT_REGEX = "^\\d$".toRegex()

        private val KDC_GENRE_MAP = mapOf(
            "0" to "총류",
            "1" to "철학",
            "2" to "종교",
            "3" to "사회과학",
            "4" to "언어",
            "5" to "과학",
            "6" to "기술과학",
            "7" to "예술",
            "8" to "문학",
            "9" to "역사"
        )
    }

    @Cacheable(
        value = ["libraryBooks"],
        key = "#externalId",
        condition = "#externalId != null && !#externalId.isEmpty()",
        unless = "#result == null"
    )
    fun searchBookByExternalId(externalId: String): ContentSearchResultDto {
        try {
            val key = externalId.removePrefix("library-")
            val results: List<LibraryBookDto> = when {
                key.matches(ISBN_10_PATTERN) || key.matches(ISBN_13_PATTERN) -> searchByIsbn(key)
                else -> searchByControlNo(key)
            }
            return results.firstOrNull()?.mapToSearchResultDto()
                ?: throw CustomException(ErrorCode.CONTENT_NOT_FOUND)
        } catch (e: CustomException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to search book by external ID: {}", externalId, e)
            throw CustomException(ErrorCode.EXTERNAL_API_ERROR)
        }
    }

    @Cacheable(
        value = ["librarySearchResults"],
        key = "'title:' + #title.trim()",
        condition = "#title != null && !#title.trim().isEmpty()"
    )
    fun searchBookByTitle(title: String): List<ContentSearchResultDto> {
        return try {
            getResultFromLibrary(title.trim())
                .take(10)
                .mapNotNull { book ->
                    runCatching { book.mapToSearchResultDto() }.getOrNull()
                }
        } catch (e: Exception) {
            logger.error("Failed to search book by title: {}", title, e)
            emptyList()
        }
    }

    private fun callLibraryApi(queryParam: String, errorContext: String): List<LibraryBookDto> {
        val response = libraryWebClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/seoji/SearchApi.do")
                    .queryParam("cert_key", libraryApiKey)
                    .queryParam("result_style", "json")
                    .queryParam("page_no", 1)
                    .queryParam("page_size", 10)
                    .query(queryParam)
                    .build()
            }
            .retrieve()
            .bodyToMono<LibraryResponse>()
            .doOnError { e -> logger.error("$errorContext 중 WebClient 오류 발생", e) }
            .onErrorMap { e -> CustomException(ErrorCode.EXTERNAL_API_ERROR) }
            .block() // 기존 동기 방식 유지를 위해 block() 사용

        return response?.docs ?: emptyList()
    }

    private fun getResultFromLibrary(title: String): List<LibraryBookDto> =
        callLibraryApi("title=$title", "도서관 API 제목 검색")

    private fun getResultFromLibraryByIsbn(isbn: String): List<LibraryBookDto> =
        callLibraryApi("isbn=$isbn", "도서관 API ISBN 검색")

    private fun getResultFromLibraryByControlNo(controlNo: String): List<LibraryBookDto> =
        callLibraryApi("control_no=$controlNo", "도서관 API CONTROL_NO 검색")


    private fun searchByIsbn(isbn: String): List<LibraryBookDto> =
        getResultFromLibraryByIsbn(isbn).filter { book ->
            book.isbn?.replace("-", "")?.trim() == isbn
        }

    private fun searchByControlNo(controlNo: String): List<LibraryBookDto> =
        getResultFromLibraryByControlNo(controlNo).filter { book ->
            book.controlNo?.trim() == controlNo
        }

    private fun LibraryBookDto.mapToSearchResultDto(): ContentSearchResultDto =
        ContentSearchResultDto(
            externalId = "library-${getLibraryExternalId()}",
            title = title?.trim(),
            creatorName = author?.cleanAuthorName(),
            description = null,
            posterUrl = titleUrl,
            releasedAt = publishPredate?.parseToLocalDateTime(),
            type = ContentType.BOOK,
            genres = extractGenres()
        )

    private fun String.cleanAuthorName(): String =
        replace(AUTHOR_PREFIX_REGEX, "")
            .replace(AUTHOR_SUFFIX_REGEX, "")
            .trim()

    private fun String.parseToLocalDateTime(): LocalDateTime? =
        takeIf { it.isNotBlank() }?.let {
            runCatching { LocalDate.parse(it, DATE_FORMATTER).atStartOfDay() }.getOrNull()
        }

    private fun LibraryBookDto.extractGenres(): List<String> = when {
        !subject.isNullOrBlank() ->
            subject.split(GENRE_SEPARATOR_REGEX)
                .mapNotNull { it.trim().takeIf(String::isNotEmpty) }
                .map { genre -> if (genre.matches(SINGLE_DIGIT_REGEX)) mapKdcToGenre(genre) else genre }

        !kdc.isNullOrBlank() ->
            listOf(mapKdcToGenre(kdc.take(1)))

        else -> emptyList()
    }

    private fun LibraryBookDto.getLibraryExternalId(): String =
        isbn?.replace("-", "")?.trim()
            ?: controlNo?.trim()
            ?: title?.trim().orEmpty().hashCode().toString()

    fun mapKdcToGenre(kdc: String): String = KDC_GENRE_MAP[kdc] ?: "기타"
}