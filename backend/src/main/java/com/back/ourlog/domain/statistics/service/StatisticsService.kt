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

@Service
class StatisticsService(
    private val statisticsRepository: StatisticsRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 통계 카드 정보를 조합하여 반환한다.
     * - 총 감상 수, 평균 평점, 선호 타입 및 감정 통계를 한 번에 조회
     *
     * @param userId 통계를 조회할 사용자 ID
     * @return StatisticsCardDto 사용자별 통계 카드 DTO
     */
    @Transactional(readOnly = true)
    fun getStatisticsCardByUserId(userId: Int): StatisticsCardDto = StatisticsCardDto(
            getTotalDiaryCount(userId),
            getAverageRating(userId),
            getFavoriteTypeAndCount(userId),
            getFavoriteEmotionAndCount(userId)
        )

    /**
     * 사용자별 총 다이어리 개수를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 다이어리 총 개수, 없으면 0L
     */
    private fun getTotalDiaryCount(userId: Int): Long = statisticsRepository.getTotalDiaryCountByUserId(userId)

    /**
     * 사용자별 평균 평점을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 평균 평점 (없으면 0.0)
     */
    private fun getAverageRating(userId: Int): Double = statisticsRepository.getAverageRatingByUserId(userId)

    /**
     * 사용자별 선호 콘텐츠 타입과 해당 개수를 조회한다.
     *
     * @param userId 사용자 ID
     * @return FavoriteTypeAndCountDto(타입, 개수), 없으면 FavoriteTypeAndCountDto("없음", 0L)
     */
    private fun getFavoriteTypeAndCount(userId: Int): FavoriteTypeAndCountDto  = statisticsRepository.findFavoriteTypeAndCountByUserId(userId)

    /**
     * 사용자별 선호 감정 태그와 해당 개수를 조회한다.
     *
     * @param userId 사용자 ID
     * @return FavoriteEmotionAndCountDto(감정, 개수), 없으면 FavoriteEmotionAndCountDto("없음", 0L)
     */
    private fun getFavoriteEmotionAndCount(userId: Int): FavoriteEmotionAndCountDto  = statisticsRepository.findFavoriteEmotionAndCountByUserId(userId)

    /**
     * 사용자별 최근 6개월 월 별 감상 수를 조회한다
     *
     * @param userId 사용자 ID
     * @return List<MonthlyDiaryCount> 최근 6개월 월 별 감상 수 리스트, 없으면 빈 리스트
     */
    @Transactional(readOnly = true)
    fun getLast6MonthsDiaryCountsByUser(userId: Int): List<MonthlyDiaryCount> {
        val startMonth = LocalDate.now().minusMonths(5).withDayOfMonth(1).atStartOfDay()
        return statisticsRepository.count6MonthlyDiaryByUserId(userId, startMonth)
    }

    /**
     * 사용자별 콘텐츠 타입 분포를 조회한다
     *
     * @param userId 사용자 ID
     * @return List<TypeCountDto> 콘텐츠 타입별 감상 수 리스트, 없으면 빈 리스트
     */
    @Transactional(readOnly = true)
    fun getTypeDistributionByUser(userId: Int): List<TypeCountDto> = statisticsRepository.findTypeCountsByUserId(userId)


    /**
     * 사용자별 콘텐츠 타입 추이 그래프, 콘텐츠 타입 순위 조회
     *
     * @param userId 사용자 ID
     * @param period 조회 기간 옵션
     * @return TypeGraphResponse(타입별 추이 그래프 리스트, 타입별 순위 리스트), 없으면 빈 리스트
     */
    @Transactional(readOnly = true)
    fun getTypeGraph(userId: Int, period: PeriodOption): TypeGraphResponse {
        val timeSet = TimeSet(period)

        val line = when (period) {
            PeriodOption.LAST_MONTH, PeriodOption.LAST_WEEK -> statisticsRepository.findTypeLineDaily(userId, timeSet.start, timeSet.end)
            else -> statisticsRepository.findTypeLineMonthly(userId, timeSet.start, timeSet.end)
        }

        val ranking = statisticsRepository.findTypeRanking(userId, timeSet.start, timeSet.end)

        return TypeGraphResponse(line, ranking)
    }

    /**
     * 사용자별 선호 장르 추이 그래프, 선호 장르 순위 조회
     *
     * @param userId 사용자 ID
     * @param period 조회 기간 옵션
     * @return GenreGraphResponse(장르별 추이 그래프 리스트, 장르별 순위 리스트), 없으면 빈 리스트
     */
    @Transactional(readOnly = true)
    fun getGenreGraph(userId: Int, period: PeriodOption): GenreGraphResponse {
        val timeSet = TimeSet(period)

        val line = when (period) {
            PeriodOption.LAST_MONTH, PeriodOption.LAST_WEEK -> statisticsRepository.findGenreLineDaily(userId, timeSet.start, timeSet.end)
            else -> statisticsRepository.findGenreLineMonthly(userId, timeSet.start, timeSet.end)
        }

        val ranking = statisticsRepository.findGenreRanking(userId, timeSet.start, timeSet.end)

        return GenreGraphResponse(line, ranking)
    }

    /**
     * 사용자별 감정 추이 그래프, 감정 순위 조회
     *
     * @param userId 사용자 ID
     * @param period 조회 기간 옵션
     * @return EmotionGraphResponse(감정별 추이 그래프 리스트, 감정별 순위 리스트), 없으면 빈 리스트
     */
    @Transactional(readOnly = true)
    fun getEmotionGraph(userId: Int, period: PeriodOption): EmotionGraphResponse {
        val timeSet = TimeSet(period)

        val line = when (period) {
            PeriodOption.LAST_MONTH, PeriodOption.LAST_WEEK -> statisticsRepository.findEmotionLineDaily(userId, timeSet.start, timeSet.end)
            else -> statisticsRepository.findEmotionLineMonthly(userId, timeSet.start, timeSet.end)
        }

        val ranking = statisticsRepository.findEmotionRanking(userId, timeSet.start, timeSet.end)

        return EmotionGraphResponse(line, ranking)
    }

    /**
     * 사용자별 OTT 추이 그래프, OTT 순위 조회
     *
     * @param userId 사용자 ID
     * @param period 조회 기간 옵션
     * @return OttGraphResponse(OTT별 추이 그래프 리스트, OTT별 순위 리스트), 없으면 빈 리스트
     */
    @Transactional(readOnly = true)
    fun getOttGraph(userId: Int, period: PeriodOption): OttGraphResponse {
        val timeSet = TimeSet(period)

        val line = when (period) {
            PeriodOption.LAST_MONTH, PeriodOption.LAST_WEEK -> statisticsRepository.findOttLineDaily(userId, timeSet.start, timeSet.end)
            else -> statisticsRepository.findOttLineMonthly(userId, timeSet.start, timeSet.end)
        }

        val ranking = statisticsRepository.findOttRanking(userId, timeSet.start, timeSet.end)

        return OttGraphResponse(line, ranking)
    }

    /**
     * 기간 옵션에 따른 시작, 끝 날짜 계산
     * - end는 항상 현재 시간 + 1일 (오늘 포함)
     */
    class TimeSet(
        val period: PeriodOption
    ) {
        val now = LocalDateTime.now()
        val start = calculateStart(period, now)
        val end = now.plusDays(1)

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
}