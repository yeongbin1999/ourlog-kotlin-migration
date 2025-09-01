package com.back.ourlog.domain.statistics.controller

import com.back.ourlog.domain.statistics.dto.EmotionGraphResponse
import com.back.ourlog.domain.statistics.dto.GenreGraphResponse
import com.back.ourlog.domain.statistics.dto.MonthlyDiaryCount
import com.back.ourlog.domain.statistics.dto.OttGraphResponse
import com.back.ourlog.domain.statistics.dto.StatisticsCardDto
import com.back.ourlog.domain.statistics.dto.TypeCountDto
import com.back.ourlog.domain.statistics.dto.TypeGraphResponse
import com.back.ourlog.domain.statistics.enums.PeriodOption
import com.back.ourlog.domain.statistics.service.StatisticsService
import com.back.ourlog.global.common.dto.RsData
import com.back.ourlog.global.common.extension.toSuccessResponse
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import com.back.ourlog.global.security.service.CustomUserDetails
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/statistics")
class StatisticsController(
    private val statisticsService: StatisticsService
) {

    @GetMapping(value = ["/card"])
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "통계 카드 조회", description = "총 감상 수, 평균 별점, 선호 장르, 주요 감정을 조회합니다.")
    fun getStatisticsCard(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<RsData<StatisticsCardDto>> {
        val userId = userDetails.id ?: throw CustomException(ErrorCode.AUTH_UNAUTHORIZED)
        return statisticsService.getStatisticsCardByUserId(userId).toSuccessResponse("통계 카드 조회 성공")
    }

    @GetMapping(value = ["/monthly-diary-graph"])
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "최근 6개월 월 별 감상 수", description = "특정 회원의 최근 6개월 월 별 감상 수를 조회합니다")
    fun getLast6MonthsDiaryCounts(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<RsData<List<MonthlyDiaryCount>>> {
        val userId = userDetails.id ?: throw CustomException(ErrorCode.AUTH_UNAUTHORIZED)
        return statisticsService.getLast6MonthsDiaryCountsByUser(userId).toSuccessResponse("최근 6개월 월 별 감상 수 조회 성공")
    }

    @GetMapping(value = ["/type-distribution"])
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "콘텐츠 타입 분포", description = "특정 회원의 콘텐츠 타입 분포를 조회합니다")
    fun getTypeDistribution(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<RsData<List<TypeCountDto>>> {
        val userId = userDetails.id ?: throw CustomException(ErrorCode.AUTH_UNAUTHORIZED)
        return statisticsService.getTypeDistributionByUser(userId).toSuccessResponse("콘텐츠 타입 분포 조회 성공")
    }

    @GetMapping("/type-graph")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "콘텐츠 타입 그래프", description = "콘텐츠 타입에 대한 그래프 데이터를 조회합니다.")
    fun getTypeGraph(
        @RequestParam period: PeriodOption,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<RsData<TypeGraphResponse>> {
        val userId = userDetails.id ?: throw CustomException(ErrorCode.AUTH_UNAUTHORIZED)
        return statisticsService.getTypeGraph(userId, period).toSuccessResponse("콘텐츠 타입 그래프 조회 성공")
    }

    @GetMapping("/genre-graph")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "장르 그래프", description = "장르에 대한 그래프 데이터를 조회합니다.")
    fun getGenreGraph(
        @RequestParam period: PeriodOption,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<RsData<GenreGraphResponse>> {
        val userId = userDetails.id ?: throw CustomException(ErrorCode.AUTH_UNAUTHORIZED)
        return statisticsService.getGenreGraph(userId, period).toSuccessResponse("장르 그래프 조회 성공")
    }

    @GetMapping("/emotion-graph")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "감정 그래프", description = "감정에 대한 그래프 데이터를 조회합니다.")
    fun getEmotionGraph(
        @RequestParam period: PeriodOption,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<RsData<EmotionGraphResponse>> {
        val userId = userDetails.id ?: throw CustomException(ErrorCode.AUTH_UNAUTHORIZED)
        return statisticsService.getEmotionGraph(userId, period).toSuccessResponse("감정 그래프 조회 성공")

    }

    @GetMapping("/ott-graph")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "OTT 그래프", description = "OTT에 대한 그래프 데이터를 조회합니다.")
    fun getOttGraph(
        @RequestParam period: PeriodOption,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<RsData<OttGraphResponse>> {
        val userId = userDetails.id ?: throw CustomException(ErrorCode.AUTH_UNAUTHORIZED)
        return statisticsService.getOttGraph(userId, period).toSuccessResponse("OTT 그래프 조회 성공")

    }
}