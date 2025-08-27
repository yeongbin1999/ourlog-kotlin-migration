package com.back.ourlog.domain.content.controller;

import com.back.ourlog.domain.content.dto.ContentResponseDto;
import com.back.ourlog.domain.content.dto.ContentSearchResultDto;
import com.back.ourlog.domain.content.entity.ContentType;
import com.back.ourlog.domain.content.service.ContentService;
import com.back.ourlog.external.common.ContentSearchFacade;
import com.back.ourlog.global.common.dto.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
@Tag(name = "컨텐츠 API")
public class ContentController {
    private final ContentService contentService;
    private final ContentSearchFacade contentSearchFacade;

    @GetMapping("/{diaryId}")
    @Operation(summary = "컨텐츠 조회")
    public ResponseEntity<RsData<ContentResponseDto>> getContent(@PathVariable("diaryId") int diaryId) {
        ContentResponseDto res = contentService.getContent(diaryId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(RsData.of("200-1", "%d번 다이어리의 조회 컨텐츠가 조회되었습니다.".formatted(diaryId), res));
    }

    @GetMapping("/search")
    @Operation(summary = "컨텐츠 검색", description = "컨텐츠를 검색합니다.")
    public ResponseEntity<RsData<List<ContentSearchResultDto>>> searchContents(
            @RequestParam ContentType type,
            @RequestParam String title
    ) {
        List<ContentSearchResultDto> results = contentSearchFacade.searchByTitle(type, title);
        return ResponseEntity.ok(RsData.of("200-1", "콘텐츠 검색 성공", results));
    }
}