package com.back.ourlog.domain.statistics.repository;

import com.back.ourlog.domain.statistics.dto.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsRepositoryCustom {

    List<MonthlyDiaryCount> countMonthlyDiaryByUserId(Integer userId, LocalDateTime startDate);

    /** 콘텐츠 타입별 월별 추이 */
    List<TypeLineGraphDto> findTypeLineMonthly(Integer userId, LocalDateTime start, LocalDateTime end);
    /** 콘텐츠 타입별 일별 추이 */
    List<TypeLineGraphDto> findTypeLineDaily(Integer userId, LocalDateTime start, LocalDateTime end);
    /** 콘텐츠 타입별 순위 */
    List<TypeRankDto> findTypeRanking(Integer userId, LocalDateTime start, LocalDateTime end);


    /** 장르별 월별 추이 */
    List<GenreLineGraphDto> findGenreLineMonthly(Integer userId, LocalDateTime start, LocalDateTime end);
    /** 장르별 일별 추이 */
    List<GenreLineGraphDto> findGenreLineDaily(Integer userId, LocalDateTime start, LocalDateTime end);
    /** 장르별 순위 */
    List<GenreRankDto> findGenreRanking(Integer userId, LocalDateTime start, LocalDateTime end);


    /** 감정별 월별 추이 */
    List<EmotionLineGraphDto> findEmotionLineMonthly(Integer userId, LocalDateTime start, LocalDateTime end);
    /** 감정별 일별 추이 */
    List<EmotionLineGraphDto> findEmotionLineDaily(Integer userId, LocalDateTime start, LocalDateTime end);
    /** 감정별 순위 */
    List<EmotionRankDto> findEmotionRanking(Integer userId, LocalDateTime start, LocalDateTime end);


    /** OTT별 월별 추이 */
    List<OttLineGraphDto> findOttLineMonthly(Integer userId, LocalDateTime start, LocalDateTime end);
    /** OTT별 일별 추이 */
    List<OttLineGraphDto> findOttLineDaily(Integer userId, LocalDateTime start, LocalDateTime end);
    /** OTT별 순위 */
    List<OttRankDto> findOttRanking(Integer userId, LocalDateTime start, LocalDateTime end);
}
