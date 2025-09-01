package com.back.ourlog.domain.content.controller

import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.domain.content.service.ContentService
import com.back.ourlog.external.common.ContentSearchFacade
import com.back.ourlog.global.common.extension.toSuccessResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/contents")
@Tag(name = "컨텐츠 API")
class ContentController(
    private val contentService: ContentService,
    private val contentSearchFacade: ContentSearchFacade
) {

    @GetMapping("/{diaryId}")
    @Operation(summary = "컨텐츠 조회")
    fun getContent(@PathVariable diaryId: Int) =
        contentService.getContent(diaryId)
            .toSuccessResponse("${diaryId}번 다이어리의 컨텐츠 조회 성공")

    @GetMapping("/search")
    @Operation(summary = "컨텐츠 검색", description = "컨텐츠를 검색합니다.")
    fun searchContents(
        @RequestParam type: ContentType,
        @RequestParam title: String
    ): ResponseEntity<*> =
        contentSearchFacade.searchByTitle(type, title)
            .toSuccessResponse("콘텐츠 검색 성공")
}
