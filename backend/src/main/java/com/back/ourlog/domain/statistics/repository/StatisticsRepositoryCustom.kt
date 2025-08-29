package com.back.ourlog.domain.statistics.repository

import com.back.ourlog.domain.statistics.dto.*
import java.time.LocalDateTime

interface StatisticsRepositoryCustom {

    /** 총 다이어리 개수 */
    fun getTotalDiaryCountByUserId(userId: Int): Long

    /** 평균 별점 */
    fun getAverageRatingByUserId(userId: Int): Double

    /** 제일 많이 본 타입 및 개수 */
    fun findFavoriteTypeAndCountByUserId(userId: Int): FavoriteTypeAndCountDto

    /** 제일 느낀 감정(Tag) 및 개수 */
    fun findFavoriteEmotionAndCountByUserId(userId: Int): FavoriteEmotionAndCountDto

    /** 최근 6개월 감상 수 */
    fun count6MonthlyDiaryByUserId(userId: Int?, startDate: LocalDateTime): List<MonthlyDiaryCount>

    /** 콘텐츠 타입별 감상 수 */
    fun findTypeCountsByUserId(userId: Int?): List<TypeCountDto>?

    /** 콘텐츠 타입별 월별 추이 */
    fun findTypeLineMonthly(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<TypeLineGraphDto>

    /** 콘텐츠 타입별 일별 추이 */
    fun findTypeLineDaily(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<TypeLineGraphDto>

    /** 콘텐츠 타입별 순위 */
    fun findTypeRanking(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<TypeRankDto>

    /** 장르별 월별 추이 */
    fun findGenreLineMonthly(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<GenreLineGraphDto>

    /** 장르별 일별 추이 */
    fun findGenreLineDaily(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<GenreLineGraphDto>

    /** 장르별 순위 */
    fun findGenreRanking(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<GenreRankDto>

    /** 감정별 월별 추이 */
    fun findEmotionLineMonthly(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<EmotionLineGraphDto>

    /** 감정별 일별 추이 */
    fun findEmotionLineDaily(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<EmotionLineGraphDto>

    /** 감정별 순위 */
    fun findEmotionRanking(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<EmotionRankDto>

    /** OTT별 월별 추이 */
    fun findOttLineMonthly(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<OttLineGraphDto>

    /** OTT별 일별 추이 */
    fun findOttLineDaily(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<OttLineGraphDto>

    /** OTT별 순위 */
    fun findOttRanking(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<OttRankDto>
}
