package com.back.ourlog.domain.timeline.controller;

import com.back.ourlog.domain.timeline.dto.TimelineResponse;
import com.back.ourlog.domain.timeline.service.TimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 프론트에서 이 API를 호출하면 공개된 일기 카드들을 받아올 수 있게 해줌..
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "타임라인 API")
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping("/timeline")
    @Operation(summary = "타임라인 조회", description = "공개된 일기 카드 조회")
    public ResponseEntity<List<TimelineResponse>> getPublicTimeline() {

        return ResponseEntity.ok(timelineService.getPublicTimeline());
    }
}
