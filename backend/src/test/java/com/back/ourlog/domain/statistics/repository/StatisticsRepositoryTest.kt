package com.back.ourlog.domain.statistics.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class StatisticsRepositoryTest @Autowired constructor(
    private val statisticsRepository: StatisticsRepository
) {

    val existUserId = 1
    val nonExistUserId = 99999

    val start = LocalDateTime.of(2000, 1, 1, 0, 0)
    val end = LocalDateTime.now()

    @Test
    @DisplayName("총 다이어리 개수 조회 성공")
    fun successfulGetTotalDiaryCountByUserId(){
        val result = statisticsRepository.getTotalDiaryCountByUserId(existUserId)
        assertThat(result).isEqualTo(8L)
    }

    @Test
    @DisplayName("총 다이어리 개수 조회 null 일때 기본값")
    fun failedGetTotalDiaryCountByUserId(){
        val result = statisticsRepository.getTotalDiaryCountByUserId(nonExistUserId)
        assertThat(result).isEqualTo(0L)
    }

    @Test
    @DisplayName("평균 별점 조회 성공")
    fun successfulGetAverageRatingByUserId(){
        val result = statisticsRepository.getAverageRatingByUserId(existUserId)
        assertThat(result).isEqualTo(2.63)
    }

    @Test
    @DisplayName("평균 별점 조회 null 일때 기본값")
    fun failedGetAverageRatingByUserId(){
        val result = statisticsRepository.getAverageRatingByUserId(nonExistUserId)
        assertThat(result).isEqualTo(0.0)
    }

    @Test
    @DisplayName("제일 많이 본 타입 및 개수 성공")
    fun successfulFindFavoriteTypeAndCountByUserId(){
        val result = statisticsRepository.findFavoriteTypeAndCountByUserId(existUserId)
        assertThat(result.favoriteType).isEqualTo("BOOK")
        assertThat(result.favoriteTypeCount).isEqualTo(3L)
    }

    @Test
    @DisplayName("제일 많이 본 타입 및 개수 null 일때 기본값")
    fun failedFindFavoriteTypeAndCountByUserId(){
        val result = statisticsRepository.findFavoriteTypeAndCountByUserId(nonExistUserId)
        assertThat(result.favoriteType).isEqualTo("없음")
        assertThat(result.favoriteTypeCount).isEqualTo(0L)
    }

    @Test
    @DisplayName("제일 느낀 감정(Tag) 및 개수 성공")
    fun successfulFindFavoriteEmotionAndCountByUserId(){
        val result = statisticsRepository.findFavoriteEmotionAndCountByUserId(existUserId)
        // 데이터가 실행 할때마다 변경되어 null 체크호 확인
        assertThat(result.favoriteEmotion).isNotNull
        assertThat(result.favoriteEmotionCount).isNotNull
    }

    @Test
    @DisplayName("제일 느낀 감정(Tag) 및 개수 null 일때 기본값")
    fun failedFindFavoriteEmotionAndCountByUserId(){
        val result = statisticsRepository.findFavoriteEmotionAndCountByUserId(nonExistUserId)
        assertThat(result.favoriteEmotion).isEqualTo("없음")
        assertThat(result.favoriteEmotionCount).isEqualTo(0L)
    }

    @Test
    @DisplayName("최근 6개월 감상 수 성공")
    fun successfulCount6MonthlyDiaryByUserId(){
        val startMonth = LocalDate.now().minusMonths(5).withDayOfMonth(1).atStartOfDay()
        val result = statisticsRepository.count6MonthlyDiaryByUserId(existUserId, startMonth)
        assertThat(result[0].period).isEqualTo("2025-09")
        assertThat(result[0].views).isEqualTo(8L)
    }

    @Test
    @DisplayName("최근 6개월 감상 수 null 일때 기본값")
    fun failedCount6MonthlyDiaryByUserId(){
        val startMonth = LocalDate.now().minusMonths(5).withDayOfMonth(1).atStartOfDay()
        val result = statisticsRepository.count6MonthlyDiaryByUserId(nonExistUserId, startMonth)
        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("콘텐츠 타입별 감상 수 성공")
    fun successfulFindTypeCountsByUserId(){
        val result = statisticsRepository.findTypeCountsByUserId(existUserId)
        assertThat(result[0].type).isEqualTo("BOOK")
        assertThat(result[0].count).isEqualTo(3L)
        assertThat(result[1].type).isEqualTo("MUSIC")
        assertThat(result[1].count).isEqualTo(3L)
    }

    @Test
    @DisplayName("콘텐츠 타입별 감상 수 null 일때 기본값")
    fun failedFindTypeCountsByUserId(){
        val result = statisticsRepository.findTypeCountsByUserId(nonExistUserId)
        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("콘텐츠 타입별 추이 성공")
    fun successfulFindTypeLineMonthly(){
        val result1 = statisticsRepository.findTypeLineMonthly(existUserId, start, end)
        val result2 = statisticsRepository.findTypeLineDaily(existUserId, start, end)
        assertThat(result1).isNotEmpty()
        assertThat(result2).isNotEmpty()
    }

    @Test
    @DisplayName("콘텐츠 타입별 추이 null 일때 기본값")
    fun failedFindTypeLineMonthly(){
        val result1 = statisticsRepository.findTypeLineMonthly(nonExistUserId, start, end)
        val result2 = statisticsRepository.findTypeLineDaily(nonExistUserId, start, end)
        assertThat(result1).isEmpty()
        assertThat(result2).isEmpty()
    }

    @Test
    @DisplayName("콘텐츠 타입별 순위 성공")
    fun successfulFindTypeRanking(){
        val result1 = statisticsRepository.findTypeRanking(existUserId, start, end)
        assertThat(result1).isNotEmpty()
    }

    @Test
    @DisplayName("콘텐츠 타입별 순위 null 일때 기본값")
    fun failedFindTypeRanking(){
        val result1 = statisticsRepository.findTypeRanking(nonExistUserId, start, end)
        assertThat(result1).isEmpty()
    }

    @Test
    @DisplayName("장르별 추이 성공")
    fun successfulFindGenreLine(){
        val result1 = statisticsRepository.findGenreLineMonthly(existUserId, start, end)
        val result2 = statisticsRepository.findGenreLineDaily(existUserId, start, end)
        assertThat(result1).isNotEmpty()
        assertThat(result2).isNotEmpty()
    }

    @Test
    @DisplayName("장르별 추이 null 일때 기본값")
    fun failedFindGenreLine(){
        val result1 = statisticsRepository.findGenreLineMonthly(nonExistUserId, start, end)
        val result2 = statisticsRepository.findGenreLineDaily(nonExistUserId, start, end)
        assertThat(result1).isEmpty()
        assertThat(result2).isEmpty()
    }

    @Test
    @DisplayName("장르별 순위 성공")
    fun successfulFindGenreRanking(){
        val result1 = statisticsRepository.findGenreRanking(existUserId, start, end)
        assertThat(result1).isNotEmpty()
    }

    @Test
    @DisplayName("장르별 순위 null 일때 기본값")
    fun failedFindGenreRanking(){
        val result1 = statisticsRepository.findGenreRanking(nonExistUserId, start, end)
        assertThat(result1).isEmpty()
    }

    @Test
    @DisplayName("감정별 추이 성공")
    fun successfulFindEmotionLine(){
        val result1 = statisticsRepository.findEmotionLineMonthly(existUserId, start, end)
        val result2 = statisticsRepository.findEmotionLineDaily(existUserId, start, end)
        assertThat(result1).isNotEmpty()
        assertThat(result2).isNotEmpty()
    }

    @Test
    @DisplayName("감정별 추이 null 일때 기본값")
    fun failedFindEmotionLine(){
        val result1 = statisticsRepository.findEmotionLineMonthly(nonExistUserId, start, end)
        val result2 = statisticsRepository.findEmotionLineDaily(nonExistUserId, start, end)
        assertThat(result1).isEmpty()
        assertThat(result2).isEmpty()
    }

    @Test
    @DisplayName("감정별 순위 성공")
    fun successfulFindEmotionRanking(){
        val result1 = statisticsRepository.findEmotionRanking(existUserId, start, end)
        assertThat(result1).isNotEmpty()
    }

    @Test
    @DisplayName("감정별 순위 null 일때 기본값")
    fun failedFindEmotionRanking(){
        val result1 = statisticsRepository.findEmotionRanking(nonExistUserId, start, end)
        assertThat(result1).isEmpty()
    }

    @Test
    @DisplayName("OTT별 추이 성공")
    fun successfulFindOttLine(){
        val result1 = statisticsRepository.findOttLineMonthly(existUserId, start, end)
        val result2 = statisticsRepository.findOttLineDaily(existUserId, start, end)
        assertThat(result1).isNotEmpty()
        assertThat(result2).isNotEmpty()
    }

    @Test
    @DisplayName("OTT별 추이 null 일때 기본값")
    fun failedFindOttLine(){
        val result1 = statisticsRepository.findOttLineMonthly(nonExistUserId, start, end)
        val result2 = statisticsRepository.findOttLineDaily(nonExistUserId, start, end)
        assertThat(result1).isEmpty()
        assertThat(result2).isEmpty()
    }

    @Test
    @DisplayName("OTT별 순위 성공")
    fun successfulFindOttRanking(){
        val result1 = statisticsRepository.findOttRanking(existUserId, start, end)
        assertThat(result1).isNotEmpty()
    }

    @Test
    @DisplayName("OTT별 순위 null 일때 기본값")
    fun failedFindOttRanking(){
        val result1 = statisticsRepository.findOttRanking(nonExistUserId, start, end)
        assertThat(result1).isEmpty()
    }
}