package com.back.ourlog.external.library.service

import com.back.ourlog.domain.content.dto.ContentSearchResultDto
import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.external.library.dto.LibraryBookDto
import com.back.ourlog.external.library.dto.LibraryResponse
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class LibraryService(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,

    @Value("\${library.api-key}")
    private val libraryApiKey: String
) {

    companion object {
        // 패턴 및 정규식 상수 정의
        private val ISBN_10_PATTERN = "\\d{10}".toRegex()
        private val ISBN_13_PATTERN = "\\d{13}".toRegex()
        private const val DATE_PATTERN = "yyyyMMdd"
        private const val AUTHOR_PREFIX_PATTERN = "(원작자|저자|지은이|글|그림|편저|엮은이)\\s*[:：]\\s*"
        private const val AUTHOR_SUFFIX_PATTERN = "\\s*;\\s*$"
        private const val GENRE_SEPARATOR_PATTERN = "[,:;]"
        private const val SINGLE_DIGIT_PATTERN = "^\\d$"

        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
        private val AUTHOR_PREFIX_REGEX = AUTHOR_PREFIX_PATTERN.toRegex()
        private val AUTHOR_SUFFIX_REGEX = AUTHOR_SUFFIX_PATTERN.toRegex()
        private val GENRE_SEPARATOR_REGEX = GENRE_SEPARATOR_PATTERN.toRegex()
        private val SINGLE_DIGIT_REGEX = SINGLE_DIGIT_PATTERN.toRegex()

        // KDC 코드 → 장르명 매핑 테이블
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

    // 외부 ID(ISBN, CONTROL_NO)로 단일 도서 조회
    @Cacheable(
        value = ["libraryBooks"],
        key = "#externalId",
        condition = "#externalId != null && !#externalId.isEmpty()",
        unless = "#result == null"
    )
    fun searchBookByExternalId(externalId: String): ContentSearchResultDto {
        return try {
            val key = externalId.removePrefix("library-")
            val results: List<LibraryBookDto> = when {
                key.matches(ISBN_10_PATTERN) || key.matches(ISBN_13_PATTERN) -> searchByIsbn(key)
                else -> searchByControlNo(key)
            }
            results.firstOrNull()?.mapToSearchResultDto()
                ?: throw CustomException(ErrorCode.CONTENT_NOT_FOUND)
        } catch (e: Exception) {
            // 캐시 오류 시 직접 API 호출로 폴백
            throw CustomException(ErrorCode.CONTENT_NOT_FOUND)
        }
    }

    // 제목으로 도서 검색 (최대 10개 반환)
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
                    runCatching {
                        book.mapToSearchResultDto()
                    }.getOrNull()
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun LibraryBookDto.mapToSearchResultDto(): ContentSearchResultDto =
        ContentSearchResultDto(
            externalId = "library-${getLibraryExternalId()}",
            title = title?.trim(),
            creatorName = author?.cleanAuthorName(),
            description = null, // 도서관 API는 설명을 제공하지 않음
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

    // 장르 추출 (SUBJECT 우선, 없으면 KDC 코드 활용)
    private fun LibraryBookDto.extractGenres(): List<String> = when {
        !subject.isNullOrBlank() ->
            subject.split(GENRE_SEPARATOR_REGEX)
                .mapNotNull { it.trim().takeIf(String::isNotEmpty) }
                .map { genre -> if (genre.matches(SINGLE_DIGIT_REGEX)) mapKdcToGenre(genre) else genre }

        !kdc.isNullOrBlank() ->
            listOf(mapKdcToGenre(kdc.take(1)))

        else -> emptyList()
    }

    // 도서의 고유 ExternalId 생성 (ISBN > CONTROL_NO > TITLE 해시)
    private fun LibraryBookDto.getLibraryExternalId(): String =
        isbn?.replace("-", "")?.trim()
            ?: controlNo?.trim()
            ?: title?.trim().orEmpty().hashCode().toString()

    private fun searchByIsbn(isbn: String): List<LibraryBookDto> =
        getResultFromLibraryByIsbn(isbn).filter { book ->
            book.isbn?.replace("-", "")?.trim() == isbn
        }

    private fun searchByControlNo(controlNo: String): List<LibraryBookDto> =
        getResultFromLibraryByControlNo(controlNo).filter { book ->
            book.controlNo?.trim() == controlNo
        }

    private fun getResultFromLibrary(title: String): List<LibraryBookDto> =
        callLibraryApi("title=${title.urlEncode()}", "도서관 API 응답 처리")

    private fun getResultFromLibraryByIsbn(isbn: String): List<LibraryBookDto> =
        callLibraryApi("isbn=${isbn.urlEncode()}", "도서관 API ISBN 검색")

    private fun getResultFromLibraryByControlNo(controlNo: String): List<LibraryBookDto> =
        callLibraryApi("control_no=${controlNo.urlEncode()}", "도서관 API CONTROL_NO 검색")

    // 공통 API 호출 메서드 (JSON → DTO 변환)
    private fun callLibraryApi(queryParam: String, errorContext: String): List<LibraryBookDto> = try {
        val url = buildApiUrl(queryParam)
        val responseData = restTemplate.getForObject(url, String::class.java)
            ?: throw RuntimeException("$errorContext 중 빈 응답")

        val response = objectMapper.readValue(responseData, LibraryResponse::class.java)
        response.docs
    } catch (e: Exception) {
        throw RuntimeException("$errorContext 중 오류 발생", e)
    }

    private fun buildApiUrl(queryParam: String): String = buildString {
        append("https://www.nl.go.kr/seoji/SearchApi.do?")
        append("cert_key=$libraryApiKey")
        append("&result_style=json")
        append("&page_no=1")
        append("&page_size=10")
        append("&$queryParam")
    }

    private fun String.urlEncode(): String = URLEncoder.encode(this, StandardCharsets.UTF_8)

    fun mapKdcToGenre(kdc: String): String = KDC_GENRE_MAP[kdc] ?: "기타"

}