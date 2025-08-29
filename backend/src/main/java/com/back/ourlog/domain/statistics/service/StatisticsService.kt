package com.back.ourlog.domain.statistics.service;

import com.back.ourlog.domain.content.entity.ContentType;
import com.back.ourlog.domain.statistics.dto.*;
import com.back.ourlog.domain.statistics.enums.PeriodOption;
import com.back.ourlog.domain.statistics.repository.StatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;

    public static final String NO_DATA_MESSAGE = "없음";
    public static final Long ZERO_COUNT = 0L;
    public static final String DEFAULT_FALLBACK_MESSAGE = "데이터 없음";

    /** 통계 카드 조회 */
    @Transactional(readOnly = true)
    public StatisticsCardDto getStatisticsCardByUserId(int userId) {


        // 제일 많이 본 타입 및 개수
        FavoriteTypeAndCountDto favoriteGenre = getFavoriteGenreAndCount(userId);

        // 제일 많이 본 감정(Tag) 및 개수
        FavoriteEmotionAndCountDto favoriteEmotion = getFavoriteEmotionAndCount(userId);

        return StatisticsCardDto.builder()
                .totalDiaryCount(getTotalDiaryCount(userId)) // 총 감상 수
                .averageRating(getAverageRating(userId)) // 평균 평점 (없으면 0.0)
                .favoriteType(favoriteGenre.getFavoriteType()) // 제일 많이 본 타입
                .favoriteTypeCount(favoriteGenre.getFavoriteTypeCount()) // 제일 많이 본 타입 개수
                .favoriteEmotion(favoriteEmotion.getFavoriteEmotion()) // 제일 많이 본 감정(Tag)
                .favoriteEmotionCount(favoriteEmotion.getFavoriteEmotionCount()) // 제일 많이 본 감정(Tag) 개수
                .build();
    }

    /** 총 다이어리 개수 */
    private long getTotalDiaryCount(int userId) {
        return statisticsRepository.getTotalDiaryCountByUserId(userId);
    }

    /** 평균 평점 (없으면 0.0) */
    private double getAverageRating(int userId) {
        return statisticsRepository.getAverageRatingByUserId(userId)
                .orElse(0.0);
    }

    /** 좋아하는 타입 및 개수 (없으면 new(없음, 0L)) */
    private FavoriteTypeAndCountDto getFavoriteGenreAndCount(int userId) {
        return statisticsRepository.findFavoriteTypeAndCountByUserId(userId)
                .orElse(new FavoriteTypeAndCountDto(NO_DATA_MESSAGE, ZERO_COUNT));
    }

    /** 좋아하는 감정(Tag) 및 개수 (없으면 new(없음, 0L)) */
    private FavoriteEmotionAndCountDto getFavoriteEmotionAndCount(int userId) {
        return statisticsRepository.findFavoriteEmotionAndCountByUserId(userId)
                .orElse(new FavoriteEmotionAndCountDto(NO_DATA_MESSAGE, ZERO_COUNT));
    }

    /** 특정 회원의 최근 6개월 월 별 감상 수 조회 */
    @Transactional(readOnly = true)
    public List<MonthlyDiaryCount> getLast6MonthsDiaryCountsByUser(Integer userId) {
        LocalDate startMonth = LocalDate.now().minusMonths(5).withDayOfMonth(1);

        // DB조회: 결과는 작성된 달에만 존재
        List<MonthlyDiaryCount> counts = statisticsRepository.countMonthlyDiaryByUserId(userId, startMonth.atStartOfDay());

        // Map으로 매핑 (period -> views)
        Map<String, Long> countMap = counts.stream()
                .collect(Collectors.toMap(
                        MonthlyDiaryCount::getPeriod,
                        MonthlyDiaryCount::getViews
                ));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        // 6개월 범위 내 모든 월에 대해 조회수 매핑, 없는 달은 0으로 초기화
        return IntStream.range(0, 6)
                .mapToObj(i -> {
                    String period = startMonth.plusMonths(i).format(formatter);
                    Long views = countMap.getOrDefault(period, ZERO_COUNT);
                    return new MonthlyDiaryCount(period, views);
                })
                .collect(Collectors.toList());
    }

    /** 특정 회원의 콘텐츠 타입 분포 조회 */
    @Transactional(readOnly = true)
    public List<TypeCountDto> getTypeDistributionByUser(int userId) {
        return statisticsRepository.findTypeCountsByUserId(userId)
                .filter(list -> !list.isEmpty())  // 값 있으면 그대로 반환
                .orElseGet(() -> Collections.singletonList(new TypeCountDto(NO_DATA_MESSAGE, 1L)));
    }

    /** 특정 회원의 콘텐츠 타입 그래프 조회 */
    @Transactional(readOnly = true)
    public TypeGraphResponse getTypeGraph(TypeGraphRequest req) {
        LocalDateTime now   = LocalDateTime.now();
        LocalDateTime start = calculateStart(req.getPeriod(), now);
        LocalDateTime end   = now.plusDays(1);

        List<TypeLineGraphDto> line;
        switch (req.getPeriod()) {
            case LAST_MONTH, LAST_WEEK -> line = statisticsRepository.findTypeLineDaily(req.getUserId(), start, end);
            default -> line = statisticsRepository.findTypeLineMonthly(req.getUserId(), start, end);
        }

        List<TypeRankDto> ranking = statisticsRepository.findTypeRanking(req.getUserId(), start, end);

        return new TypeGraphResponse(line, ranking);
    }

    /** 특정 회원의 장르 타입 그래프 조회 */
    @Transactional(readOnly = true)
    public GenreGraphResponse getGenreGraph(int userId, PeriodOption period) {
        StopWatch stopWatch = new StopWatch();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = calculateStart(period, now);
        LocalDateTime end = now.plusDays(1);

        List<GenreLineGraphDto> line;
        switch (period) {
            case LAST_MONTH, LAST_WEEK -> {
                stopWatch.start("findGenreLineDaily");
                line = statisticsRepository.findGenreLineDaily(userId, start, end);
                stopWatch.stop();
            }
            default -> {
                stopWatch.start("findGenreLineMonthly");
                line = statisticsRepository.findGenreLineMonthly(userId, start, end);
                stopWatch.stop();
            }
        }

        stopWatch.start("findGenreRanking");
        List<GenreRankDto> ranking = statisticsRepository.findGenreRanking(userId, start, end);
        stopWatch.stop();

        log.info("StatisticsService.getGenreGraph - StopWatch: {}", stopWatch.prettyPrint());
        return new GenreGraphResponse(line, ranking);
    }

    /** 특정 회원의 감정 그래프 조회 */
    public EmotionGraphResponse getEmotionGraph(int userId, PeriodOption period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = calculateStart(period, now);
        LocalDateTime end = now.plusDays(1);

        List<EmotionLineGraphDto> line;
        switch (period) {
            case LAST_MONTH, LAST_WEEK -> line = statisticsRepository.findEmotionLineDaily(userId, start, end);
            default -> line = statisticsRepository.findEmotionLineMonthly(userId, start, end);
        }

        List<EmotionRankDto> ranking = statisticsRepository.findEmotionRanking(userId, start, end);

        return new EmotionGraphResponse(line, ranking);
    }

    /** 특정 회원의 OTT 그래프 조회 */
    public OttGraphResponse getOttGraph(int userId, PeriodOption period) {
        LocalDateTime now   = LocalDateTime.now();
        LocalDateTime start = calculateStart(period, now);
        LocalDateTime end   = now.plusDays(1);

        List<OttLineGraphDto> line = switch (period) {
            case LAST_MONTH, LAST_WEEK -> statisticsRepository.findOttLineDaily(userId, start, end);
            default                    -> statisticsRepository.findOttLineMonthly(userId, start, end);
        };

        List<OttRankDto> ranking = statisticsRepository.findOttRanking(userId, start, end);

        return new OttGraphResponse(line, ranking);
    }

    private LocalDateTime calculateStart(PeriodOption period, LocalDateTime now) {
        return switch (period) {
            case THIS_YEAR    -> now.withDayOfYear(1);
            case LAST_6_MONTHS -> now.minusMonths(5).withDayOfMonth(1);
            case LAST_MONTH   -> now.minusMonths(1).withDayOfMonth(1);
            case LAST_WEEK    -> now.minusWeeks(1);
            default           -> LocalDateTime.of(1970,1,1,0,0);
        };
    }

}
