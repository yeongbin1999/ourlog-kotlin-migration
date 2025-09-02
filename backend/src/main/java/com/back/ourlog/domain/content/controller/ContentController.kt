package com.back.ourlog.domain.content.controller

import com.back.ourlog.domain.content.dto.ContentResponseDto
import com.back.ourlog.domain.content.dto.ContentSearchResultDto
import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.domain.content.service.ContentService
import com.back.ourlog.external.common.ContentSearchFacade
import com.back.ourlog.global.common.dto.RsData
import com.back.ourlog.global.common.extension.toSuccessResponse
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import com.back.ourlog.global.rq.Rq
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/api/v1/contents")
@Tag(name = "컨텐츠 API")
class ContentController(
    private val contentService: ContentService,
    private val contentSearchFacade: ContentSearchFacade,
    private val rq: Rq
) {

    @GetMapping("/{diaryId}")
    @Operation(summary = "컨텐츠 조회")
    fun getContentForDiary(
        @PathVariable diaryId: Int
    ): ResponseEntity<RsData<ContentResponseDto>> {
        val user = rq.currentUser ?: throw CustomException(ErrorCode.AUTH_UNAUTHORIZED)
        val content = contentService.getContent(diaryId, user.id)

        return content.toSuccessResponse("${diaryId}번 다이어리의 컨텐츠 조회 성공")
    }

    @GetMapping("/search")
    @Operation(summary = "컨텐츠 검색", description = "컨텐츠를 검색합니다.")
    fun searchContents(
        @RequestParam type: ContentType,
        @RequestParam @NotBlank(message = "검색어(title)는 비어 있을 수 없습니다.") title: String
    ): ResponseEntity<RsData<List<ContentSearchResultDto>>> {
        return contentSearchFacade.searchByTitle(type, title)
            .toSuccessResponse("콘텐츠 검색 성공")
    }

}
