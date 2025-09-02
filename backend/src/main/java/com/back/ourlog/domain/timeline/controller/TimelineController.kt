package com.back.ourlog.domain.timeline.controller

import com.back.ourlog.domain.timeline.service.TimelineService
import com.back.ourlog.global.common.extension.toSuccessResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// 프론트에서 이 API를 호출하면 공개된 일기 카드들을 받아올 수 있게 해줌..
@RestController
@RequestMapping("/api/v1")
@Tag(name = "타임라인 API")
class TimelineController(
    private val timelineService: TimelineService // 1. 주 생성자로 DI, Lombok 제거
) {

    @GetMapping("/timeline")
    @Operation(summary = "타임라인 조회", description = "공개된 일기 카드 조회")
    // 2. 서비스 호출 결과에 .toSuccessResponse()를 붙여 바로 반환
    fun getPublicTimeline() = timelineService.getPublicTimeline()
        .toSuccessResponse("타임라인 조회 성공")
}