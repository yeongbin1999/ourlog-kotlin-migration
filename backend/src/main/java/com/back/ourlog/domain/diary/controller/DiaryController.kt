package com.back.ourlog.domain.diary.controller

import com.back.ourlog.domain.diary.dto.DiaryDetailDto
import com.back.ourlog.domain.diary.dto.DiaryResponseDto
import com.back.ourlog.domain.diary.dto.DiaryResponseDto.Companion.from
import com.back.ourlog.domain.diary.dto.DiaryUpdateRequestDto
import com.back.ourlog.domain.diary.dto.DiaryWriteRequestDto
import com.back.ourlog.domain.diary.service.DiaryService
import com.back.ourlog.global.common.dto.RsData
import com.back.ourlog.global.common.extension.toSuccessResponse
import com.back.ourlog.global.common.extension.toSuccessResponseWithoutData
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import com.back.ourlog.global.rq.Rq
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/diaries")
@Tag(name = "감상일기 API")
class DiaryController(
    private val diaryService: DiaryService,
    private val rq: Rq
) {
    @PostMapping
    @Operation(summary = "감상일기 등록", description = "감상일기를 작성합니다.")
    fun writeDiary(
        @RequestBody @Valid req: DiaryWriteRequestDto
    ): ResponseEntity<RsData<DiaryResponseDto>> {
        val user = rq.currentUser ?: throw CustomException(ErrorCode.AUTH_UNAUTHORIZED)

        val diary = diaryService.writeWithContentSearch(req, user)

        return from(diary).toSuccessResponse("감상일기가 등록되었습니다.")
    }

    @GetMapping("/{diaryId}")
    @Operation(summary = "감상일기 조회", description = "감상일기를 조회합니다.")
    fun getDiary(
        @PathVariable("diaryId") diaryId: Int
    ): ResponseEntity<RsData<DiaryDetailDto>> {
        val diaryDetailDto = diaryService.getDiaryDetail(diaryId)

        return diaryDetailDto.toSuccessResponse("${diaryId}번 감상일기가 조회되었습니다.")
    }

    @PutMapping("/{diaryId}")
    @Operation(summary = "감상일기 수정", description = "감상일기를 수정합니다.")
    fun updateDiary(
        @PathVariable diaryId: Int,
        @RequestBody @Valid req: DiaryUpdateRequestDto
    ): ResponseEntity<RsData<DiaryResponseDto>> {
        val user = rq.currentUser ?: throw CustomException(ErrorCode.AUTH_UNAUTHORIZED)

        val result = diaryService.update(diaryId, req, user)

        return result.toSuccessResponse("일기 수정 완료")
    }

    @DeleteMapping("/{diaryId}")
    @Operation(summary = "감상일기 삭제", description = "감상일기를 삭제합니다.")
    @PreAuthorize("#userId == authentication.principal.id")
    fun deleteDiary(
        @PathVariable diaryId: Int
    ): ResponseEntity<RsData<Void>> {
        val user = rq.currentUser ?: throw CustomException(ErrorCode.AUTH_UNAUTHORIZED)

        diaryService.delete(diaryId, user)

        return toSuccessResponseWithoutData("일기 삭제 완료")
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "내 다이어리 목록 조회", description = "사용자의 감상일기 목록을 페이징 조회합니다.")
    fun getMyDiaries(
        @PathVariable userId: Int,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<RsData<Page<DiaryResponseDto>>> {
        val diaries = diaryService.getDiariesByUser(userId, pageable)

        return diaries.toSuccessResponse("내 다이어리 목록 조회 성공")
    }
}