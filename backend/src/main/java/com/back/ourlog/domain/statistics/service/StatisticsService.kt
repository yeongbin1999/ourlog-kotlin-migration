package com.back.ourlog.domain.statistics.service

import com.back.ourlog.domain.statistics.dto.EmotionGraphResponse
import com.back.ourlog.domain.statistics.dto.FavoriteEmotionAndCountDto
import com.back.ourlog.domain.statistics.dto.FavoriteTypeAndCountDto
import com.back.ourlog.domain.statistics.dto.GenreGraphResponse
import com.back.ourlog.domain.statistics.dto.GenreLineGraphDto
import com.back.ourlog.domain.statistics.dto.MonthlyDiaryCount
import com.back.ourlog.domain.statistics.dto.OttGraphResponse
import com.back.ourlog.domain.statistics.dto.StatisticsCardDto
import com.back.ourlog.domain.statistics.dto.TypeCountDto
import com.back.ourlog.domain.statistics.dto.TypeGraphResponse
import com.back.ourlog.domain.statistics.enums.PeriodOption
import com.back.ourlog.domain.statistics.repository.StatisticsRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StopWatch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class StatisticsService(
    private val statisticsRepository: StatisticsRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val NO_DATA_MESSAGE = "없음"
        const val ZERO_COUNT = 0L
    }

    /** 통계 카드 조회 */
    fun getStatisticsCardByUserId(userId: Int): StatisticsCardDto = StatisticsCardDto(
            getTotalDiaryCount(userId),
            getAverageRating(userId),
            getFavoriteTypeAndCount(userId),
            getFavoriteEmotionAndCount(userId)
        )

    /** 총 다이어리 개수 */
    private fun getTotalDiaryCount(userId: Int): Long = statisticsRepository.getTotalDiaryCountByUserId(userId)

    /** 평균 평점 (없으면 0.0) */
    private fun getAverageRating(userId: Int): Double = statisticsRepository.getAverageRatingByUserId(userId)

    /** 좋아하는 타입 및 개수 (없으면 new(없음, 0L)) */
    private fun getFavoriteTypeAndCount(userId: Int): FavoriteTypeAndCountDto  = statisticsRepository.findFavoriteTypeAndCountByUserId(userId)

    /** 좋아하는 감정(Tag) 및 개수 (없으면 new(없음, 0L)) */
    private fun getFavoriteEmotionAndCount(userId: Int): FavoriteEmotionAndCountDto  = statisticsRepository.findFavoriteEmotionAndCountByUserId(userId)

    /** 특정 회원의 최근 6개월 월 별 감상 수 조회 */
    fun getLast6MonthsDiaryCountsByUser(userId: Int?): List<MonthlyDiaryCount> {
        val startMonth = LocalDate.now().minusMonths(5).withDayOfMonth(1)

        // DB조회: 결과는 작성된 달에만 존재
        val counts = statisticsRepository.count6MonthlyDiaryByUserId(userId, startMonth.atStartOfDay())

        // Map으로 매핑 (period -> views)
        val countMap = counts.associate { it.period to it.views }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM")

        // 6개월 범위 내 모든 월에 대해 조회수 매핑, 없는 달은 0으로 초기화
        return (0..5).map { i ->
            val period = startMonth.plusMonths(i.toLong()).format(formatter)
            val views = countMap.getOrDefault(period, ZERO_COUNT)
            MonthlyDiaryCount(period, views)
        }
    }

    /** 특정 회원의 콘텐츠 타입 분포 조회 */
    fun getTypeDistributionByUser(userId: Int): List<TypeCountDto> {
        val result = statisticsRepository.findTypeCountsByUserId(userId)
        return if (result.isNullOrEmpty()) {
            listOf(TypeCountDto(NO_DATA_MESSAGE, ZERO_COUNT))
        } else result
    }

    /** 특정 회원의 콘텐츠 타입 그래프 조회 */
    fun getTypeGraph(userId: Int, period: PeriodOption): TypeGraphResponse {
        val now = LocalDateTime.now()
        val start = calculateStart(period, now)
        val end = now.plusDays(1)

        val line = when (period) {
            PeriodOption.LAST_MONTH, PeriodOption.LAST_WEEK -> statisticsRepository.findTypeLineDaily(userId, start, end)
            else -> statisticsRepository.findTypeLineMonthly(userId, start, end)
        }

        val ranking = statisticsRepository.findTypeRanking(userId, start, end)

        return TypeGraphResponse(line, ranking)
    }

    /** 특정 회원의 장르 타입 그래프 조회 */
    fun getGenreGraph(userId: Int, period: PeriodOption): GenreGraphResponse {
        val stopWatch = StopWatch()
        val now = LocalDateTime.now()
        val start = calculateStart(period, now)
        val end = now.plusDays(1)

        val line: List<GenreLineGraphDto>
        when (period) {
            PeriodOption.LAST_MONTH, PeriodOption.LAST_WEEK -> {
                stopWatch.start("findGenreLineDaily")
                line = statisticsRepository.findGenreLineDaily(userId, start, end)
                stopWatch.stop()
            }
            else -> {
                stopWatch.start("findGenreLineMonthly")
                line = statisticsRepository.findGenreLineMonthly(userId, start, end)
                stopWatch.stop()
            }
        }

        stopWatch.start("findGenreRanking")
        val ranking = statisticsRepository.findGenreRanking(userId, start, end)
        stopWatch.stop()

        log.info("StatisticsService.getGenreGraph - StopWatch: {}", stopWatch.prettyPrint())
        return GenreGraphResponse(line, ranking)
    }

    /** 특정 회원의 감정 그래프 조회 */
    fun getEmotionGraph(userId: Int, period: PeriodOption): EmotionGraphResponse {
        val now = LocalDateTime.now()
        val start = calculateStart(period, now)
        val end = now.plusDays(1)

        val line = when (period) {
            PeriodOption.LAST_MONTH, PeriodOption.LAST_WEEK -> statisticsRepository.findEmotionLineDaily(userId, start, end)
            else -> statisticsRepository.findEmotionLineMonthly(userId, start, end)
        }

        val ranking = statisticsRepository.findEmotionRanking(userId, start, end)

        return EmotionGraphResponse(line, ranking)
    }

    /** 특정 회원의 OTT 그래프 조회 */
    fun getOttGraph(userId: Int, period: PeriodOption): OttGraphResponse {
        val now = LocalDateTime.now()
        val start = calculateStart(period, now)
        val end = now.plusDays(1)

        val line = when (period) {
            PeriodOption.LAST_MONTH, PeriodOption.LAST_WEEK -> statisticsRepository.findOttLineDaily(userId, start, end)
            else -> statisticsRepository.findOttLineMonthly(userId, start, end)
        }

        val ranking = statisticsRepository.findOttRanking(userId, start, end)

        return OttGraphResponse(line, ranking)
    }

    private fun calculateStart(period: PeriodOption, now: LocalDateTime): LocalDateTime {
        return when (period) {
            PeriodOption.THIS_YEAR -> now.withDayOfYear(1)
            PeriodOption.LAST_6_MONTHS -> now.minusMonths(5).withDayOfMonth(1)
            PeriodOption.LAST_MONTH -> now.minusMonths(1).withDayOfMonth(1)
            PeriodOption.LAST_WEEK -> now.minusWeeks(1)
            else -> LocalDateTime.of(1970, 1, 1, 0, 0)
        }
    }
}