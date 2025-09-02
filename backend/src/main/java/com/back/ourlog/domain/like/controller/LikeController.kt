package com.back.ourlog.domain.like.controller

import com.back.ourlog.domain.like.dto.LikeCountResponse
import com.back.ourlog.domain.like.dto.LikeResponse
import com.back.ourlog.domain.like.service.LikeService
import com.back.ourlog.global.common.extension.toSuccessResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/likes")
@Tag(name = "좋아요 API")
class LikeController(
    private val likeService: LikeService,
) {

    @PostMapping("/{diaryId}")
    @Operation(summary = "좋아요 등록")
    fun like(@PathVariable diaryId: Int): ResponseEntity<*> { // 반환 타입을 명시적으로 선언
        val liked = likeService.like(diaryId)
        val likeCount = likeService.getLikeCount(diaryId)
        val responseDto = LikeResponse(liked, likeCount)

        return responseDto.toSuccessResponse("좋아요 상태가 변경되었습니다.")
    }

    @DeleteMapping("/{diaryId}")
    @Operation(summary = "좋아요 취소")
    fun unlike(@PathVariable diaryId: Int): ResponseEntity<*> {
        likeService.unlike(diaryId)
        val likeCount = likeService.getLikeCount(diaryId)
        val responseDto = LikeResponse(liked = false, likeCount = likeCount)

        return responseDto.toSuccessResponse("좋아요가 취소되었습니다.")
    }

    @GetMapping("/count")
    @Operation(summary = "좋아요 수 단건 조회")
    fun getLikeCount(@RequestParam diaryId: Int) = // 이 메서드는 한 줄로 가능
        likeService.getLikeCount(diaryId).toSuccessResponse("좋아요 수를 조회했습니다.")
}