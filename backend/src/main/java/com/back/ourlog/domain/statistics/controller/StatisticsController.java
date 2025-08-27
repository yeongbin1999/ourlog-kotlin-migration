package com.back.ourlog.domain.statistics.controller;

import com.back.ourlog.domain.statistics.dto.*;
import com.back.ourlog.domain.statistics.enums.PeriodOption;
import com.back.ourlog.domain.statistics.service.StatisticsService;
import com.back.ourlog.global.security.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping(value = "/card")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "통계 카드 조회", description = "총 감상 수, 평균 별정, 선호 장르, 주요 감정을 조회합니다.")
    public ResponseEntity<StatisticsCardDto> getStatisticsCard(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(statisticsService.getStatisticsCardByUserId(userDetails.getId()));
    }

    @GetMapping(value = "/monthly-diary-graph")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "최근 6개월 월 별 감상 수", description = "특정 회원의 최근 6개월 월 별 감상 수를 조회합니다")
    public ResponseEntity<List<MonthlyDiaryCount>> getLast6MonthsDiaryCounts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(statisticsService.getLast6MonthsDiaryCountsByUser(userDetails.getId()));
    }

    @GetMapping(value = "/type-distribution")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "콘테츠 타입 분포", description = "특정 회원의 콘테츠 타입 분포를 조회합니다")
    public ResponseEntity<List<TypeCountDto>> getTypeDistribution(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(statisticsService.getTypeDistributionByUser(userDetails.getId()));
    }

    @GetMapping("/type-graph")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "콘텐츠 타입 그래프", description = "콘텐츠 타입에 대한 그래프 데이터를 조회합니다.")
    public ResponseEntity<TypeGraphResponse> getTypeGraph(@RequestParam PeriodOption period, @AuthenticationPrincipal CustomUserDetails userDetails) {
        TypeGraphRequest req = new TypeGraphRequest(userDetails.getId(), period);
        return ResponseEntity.ok(statisticsService.getTypeGraph(req));
    }

    @GetMapping("/genre-graph")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "장르 그래프", description = "장르에 대한 그래프 데이터를 조회합니다.")
    public ResponseEntity<GenreGraphResponse> getGenreGraph(@RequestParam PeriodOption period, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(statisticsService.getGenreGraph(userDetails.getId(), period));
    }

    @GetMapping("/emotion-graph")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "감정 그래프", description = "감정에 대한 그래프 데이터를 조회합니다.")
    public ResponseEntity<EmotionGraphResponse> getEmotionGraph(@RequestParam PeriodOption period, @AuthenticationPrincipal CustomUserDetails userDetails) {
        EmotionGraphResponse res = statisticsService.getEmotionGraph(userDetails.getId(), period);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/ott-graph")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "OTT 그래프", description = "OTT에 대한 그래프 데이터를 조회합니다.")
    public ResponseEntity<OttGraphResponse> getOttGraph(@RequestParam PeriodOption period, @AuthenticationPrincipal CustomUserDetails userDetails) {
        OttGraphResponse res = statisticsService.getOttGraph(userDetails.getId(), period);
        return ResponseEntity.ok(res);
    }
}
