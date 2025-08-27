package com.back.ourlog.domain.diary.controller;

import com.back.ourlog.domain.diary.dto.DiaryDetailDto;
import com.back.ourlog.domain.diary.dto.DiaryResponseDto;
import com.back.ourlog.domain.diary.dto.DiaryUpdateRequestDto;
import com.back.ourlog.domain.diary.dto.DiaryWriteRequestDto;
import com.back.ourlog.domain.diary.entity.Diary;
import com.back.ourlog.domain.diary.service.DiaryService;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.global.common.dto.RsData;
import com.back.ourlog.global.exception.CustomException;
import com.back.ourlog.global.exception.ErrorCode;
import com.back.ourlog.global.rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/diaries")
@Tag(name = "감상일기 API")
public class DiaryController {

    private final DiaryService diaryService;
    private final Rq rq;

    @PostMapping
    @Operation(summary = "감상일기 등록", description = "감상일기를 작성합니다.")
    public ResponseEntity<RsData<DiaryResponseDto>> writeDiary(
            @Valid @RequestBody DiaryWriteRequestDto req
    ) {
        User user = rq.getCurrentUser();
        if (user == null) throw new CustomException(ErrorCode.AUTH_UNAUTHORIZED);

        Diary diary = diaryService.writeWithContentSearch(req, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RsData.of("201-1", "감상일기가 등록되었습니다.", DiaryResponseDto.from(diary)));
    }

    @GetMapping("/{diaryId}")
    @Operation(summary = "감상일기 조회", description = "감상일기를 조회합니다.")
    public ResponseEntity<RsData<DiaryDetailDto>> getDiary(@PathVariable("diaryId") int diaryId) {
        DiaryDetailDto diaryDetailDto = diaryService.getDiaryDetail(diaryId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(RsData.of("200-1", "%d번 감상일기가 조회되었습니다.".formatted(diaryId), diaryDetailDto));
    }

    @PutMapping("/{diaryId}")
    @Operation(summary = "감상일기 수정", description = "감상일기를 수정합니다.")

    public ResponseEntity<RsData<DiaryResponseDto>> updateDiary(
            @PathVariable int diaryId,
            @Valid @RequestBody DiaryUpdateRequestDto req
    ) {
        User user = rq.getCurrentUser();
        if (user == null) throw new CustomException(ErrorCode.AUTH_UNAUTHORIZED);

        DiaryResponseDto result = diaryService.update(diaryId, req, user);
        return ResponseEntity.ok(RsData.of("200-0", "일기 수정 완료", result));
    }

    @DeleteMapping("/{diaryId}")
    @Operation(summary = "감상일기 삭제", description = "감상일기를 삭제합니다.")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<RsData<Void>> deleteDiary(@PathVariable int diaryId) {
        User user = rq.getCurrentUser();
        if (user == null) throw new CustomException(ErrorCode.AUTH_UNAUTHORIZED);

        diaryService.delete(diaryId, user);
        return ResponseEntity.ok(RsData.of("200-0", "일기 삭제 완료", null));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "내 다이어리 목록 조회", description = "사용자의 감상일기 목록을 페이징 조회합니다.")
    public ResponseEntity<RsData<Page<DiaryResponseDto>>> getMyDiaries(
            @PathVariable Integer userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<DiaryResponseDto> diaries = diaryService.getDiariesByUser(userId, pageable);
        return ResponseEntity.ok(RsData.of("200", "내 다이어리 목록 조회 성공", diaries));
    }
}