package com.back.ourlog.external.library.service;

import com.back.ourlog.domain.content.dto.ContentSearchResultDto;
import com.back.ourlog.domain.content.entity.ContentType;
import com.back.ourlog.global.exception.CustomException;
import com.back.ourlog.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${library.api-key}")
    private String libraryApiKey;

    public ContentSearchResultDto searchBookByExternalId(String externalId) {
        String key = externalId.replace("library-", "");
        List<Map<String, Object>> results;

        if (key.matches("\\d{10}") || key.matches("\\d{13}")) {
            results = getResultFromLibraryByIsbn(key).stream()
                    .filter(doc -> {
                        String eaIsbn = (String) doc.get("EA_ISBN");
                        boolean match = eaIsbn != null && eaIsbn.replaceAll("-", "").trim().equals(key);
                        return match;
                    })
                    .toList();
        } else {
            results = getResultFromLibraryByControlNo(key).stream()
                    .filter(doc -> {
                        String controlNo = (String) doc.get("CONTROL_NO");
                        boolean match = controlNo != null && controlNo.trim().equals(key);
                        return match;
                    })
                    .toList();
        }

        return results.stream()
                .findFirst()
                .map(this::mapToSearchResultDto)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND));
    }

    public List<ContentSearchResultDto> searchBookByTitle(String title) {
        List<Map<String, Object>> docs = getResultFromLibrary(title);

        return docs.stream()
                .limit(10)
                .map(item -> {
                    String externalId = "library-" + getLibraryExternalId(item);
                    try {
                        return searchBookByExternalId(externalId);
                    } catch (CustomException e) {
                        return mapToSearchResultDto(item); // fallback
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private ContentSearchResultDto mapToSearchResultDto(Map<String, Object> item) {
        String bookTitle = (String) item.get("TITLE");
        String creatorName = cleanAuthorPrefix((String) item.get("AUTHOR"));
        String posterUrl = (String) item.get("TITLE_URL");
        String releasedAtStr = (String) item.get("PUBLISH_PREDATE");

        // 설명은 책에서는 저장하지 않으므로 null로 처리
        String description = null;

        LocalDateTime releasedAt = null;
        if (releasedAtStr != null && !releasedAtStr.isBlank()) {
            try {
                releasedAt = LocalDate.parse(releasedAtStr, DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay();
            } catch (DateTimeParseException ignored) {}
        }

        List<String> genres = extractGenres(item);

        return new ContentSearchResultDto(
                "library-" + getLibraryExternalId(item),
                bookTitle,
                creatorName,
                description,
                posterUrl,
                releasedAt,
                ContentType.BOOK,
                genres
        );
    }

    private String cleanAuthorPrefix(String rawAuthor) {
        if (rawAuthor == null) return null;

        return rawAuthor
                .replaceAll("(원작자|저자|지은이|글|그림|편저|엮은이)\\s*[:：]\\s*", "")  // 콜론(:, ：) 처리
                .replaceAll("\\s*;\\s*$", "") // 맨 끝 세미콜론 제거
                .trim();
    }

    private List<String> extractGenres(Map<String, Object> item) {
        String subject = (String) item.get("SUBJECT");
        String kdc = (String) item.get("KDC");
        String classNo = (String) item.get("class_no");

        List<String> genres = new ArrayList<>();

        if (subject != null && !subject.isBlank()) {
            List<String> subjectList = Arrays.stream(subject.split("[,;]"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            for (String s : subjectList) {
                if (s.matches("^\\d$")) {
                    genres.add(mapKdcToGenre(s));
                } else {
                    genres.add(s);
                }
            }
        } else if (kdc != null || classNo != null) {
            String code = kdc != null ? kdc : classNo;
            if (code.length() >= 1) {
                String mainCategory = code.substring(0, 1);
                genres.add(mapKdcToGenre(mainCategory));
            }
        }

        return genres;
    }

    public String mapKdcToGenre(String kdc) {
        return switch (kdc) {
            case "0" -> "총류";
            case "1" -> "철학";
            case "2" -> "종교";
            case "3" -> "사회과학";
            case "4" -> "언어";
            case "5" -> "과학";
            case "6" -> "기술과학";
            case "7" -> "예술";
            case "8" -> "문학";
            case "9" -> "역사";
            default -> "기타";
        };
    }

    private String getLibraryExternalId(Map<String, Object> item) {
        String isbn = (String) item.get("EA_ISBN");
        String controlNo = (String) item.get("CONTROL_NO");

        if (isbn != null && !isbn.isBlank()) {
            return isbn.replaceAll("-", "").trim();
        } else if (controlNo != null && !controlNo.isBlank()) {
            return controlNo.trim();
        } else {
            return String.valueOf(((String) item.get("TITLE")).hashCode());
        }
    }

    private List<Map<String, Object>> getResultFromLibrary(String bookTitle) {
        try {
            String url = "https://www.nl.go.kr/seoji/SearchApi.do?cert_key=%s".formatted(libraryApiKey) +
                    "&result_style=json&page_no=1&page_size=10&title=%s".formatted(bookTitle);

            String data = restTemplate.getForObject(url, String.class);
            Map map = objectMapper.readValue(data, Map.class);

            return (List<Map<String, Object>>) map.get("docs");
        } catch (Exception e) {
            throw new RuntimeException("도서관 API 응답 처리 중 오류 발생", e);
        }
    }

    private List<Map<String, Object>> getResultFromLibraryByIsbn(String isbn) {
        try {
            String url = String.format(
                    "https://www.nl.go.kr/seoji/SearchApi.do?cert_key=%s&result_style=json&page_no=1&page_size=10&isbn=%s",
                    libraryApiKey,
                    isbn
            );

            String data = restTemplate.getForObject(url, String.class);
            Map map = objectMapper.readValue(data, Map.class);

            List<Map<String, Object>> docs = (List<Map<String, Object>>) map.get("docs");

            return docs.stream()
                    .filter(doc -> {
                        String eaIsbn = (String) doc.get("EA_ISBN");
                        return eaIsbn != null && eaIsbn.replaceAll("-", "").trim().equals(isbn);
                    })
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("도서관 API ISBN 검색 오류", e);
        }
    }

    private List<Map<String, Object>> getResultFromLibraryByControlNo(String controlNo) {
        try {
            String url = "https://www.nl.go.kr/seoji/SearchApi.do?cert_key=%s".formatted(libraryApiKey) +
                    "&result_style=json&page_no=1&page_size=10&control_no=%s".formatted(controlNo);

            String data = restTemplate.getForObject(url, String.class);
            Map map = objectMapper.readValue(data, Map.class);
            return (List<Map<String, Object>>) map.get("docs");
        } catch (Exception e) {
            throw new RuntimeException("도서관 API CONTROL_NO 검색 오류", e);
        }
    }

}
