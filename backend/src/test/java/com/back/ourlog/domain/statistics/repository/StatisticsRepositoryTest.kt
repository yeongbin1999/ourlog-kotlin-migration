package com.back.ourlog.domain.statistics.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class StatisticsRepositoryTest @Autowired constructor(
    private val statisticsRepository: StatisticsRepository
) {

    val existUserId = 1
    val nonExistUserId = 99999

    @Test
    @DisplayName("총 다이어리 개수 조회 성공")
    fun successfulGetTotalDiaryCountByUserId(){
        val result = statisticsRepository.getTotalDiaryCountByUserId(existUserId)
        assertThat(result).isEqualTo(2L)
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
        assertThat(result).isEqualTo(3.5)
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
        assertThat(result.favoriteTypeCount).isEqualTo(1L)
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
}