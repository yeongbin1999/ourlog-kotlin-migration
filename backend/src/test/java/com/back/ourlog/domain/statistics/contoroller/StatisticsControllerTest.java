package com.back.ourlog.domain.statistics.contoroller;

import com.back.ourlog.domain.statistics.controller.StatisticsController;
import com.back.ourlog.domain.statistics.dto.*;
import com.back.ourlog.domain.statistics.enums.PeriodOption;
import com.back.ourlog.domain.statistics.service.StatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class StatisticsControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private StatisticsService statisticsService;

    @Test
    @DisplayName("통계 카드 조회")
    @WithUserDetails("user1@test.com")
    void 통계_카드_조회() throws Exception {

        ResultActions resultActions = mvc.perform(
                get("/api/v1/statistics/card")
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(StatisticsController.class))
                .andExpect(handler().methodName("getStatisticsCard"))
                .andExpect(status().isOk());

        StatisticsCardDto statisticsCardDto = statisticsService.getStatisticsCardByUserId(1);

        resultActions
                .andExpect(jsonPath("$.totalDiaryCount").value(statisticsCardDto.getTotalDiaryCount()))
                .andExpect(jsonPath("$.averageRating").value(statisticsCardDto.getAverageRating()))
                .andExpect(jsonPath("$.favoriteType").value(statisticsCardDto.getFavoriteType()))
                .andExpect(jsonPath("$.favoriteTypeCount").value(statisticsCardDto.getFavoriteTypeCount()))
                .andExpect(jsonPath("$.favoriteEmotion").value(statisticsCardDto.getFavoriteEmotion()))
                .andExpect(jsonPath("$.favoriteEmotionCount").value(statisticsCardDto.getFavoriteEmotionCount()));

        System.out.println("Total Diary Count: " + statisticsCardDto.getTotalDiaryCount());
        System.out.println("Average Rating: " + statisticsCardDto.getAverageRating());
        System.out.println("Favorite Type: " + statisticsCardDto.getFavoriteType());
        System.out.println("Favorite Type Count: " + statisticsCardDto.getFavoriteTypeCount());
        System.out.println("Favorite Emotion: " + statisticsCardDto.getFavoriteEmotion());
        System.out.println("Favorite Emotion Count: " + statisticsCardDto.getFavoriteEmotionCount());

    }

    @Test
    @DisplayName("최근 6개월 월 별 감상 수 조회")
    @WithUserDetails("user1@test.com")
    void 최근_6개월_월_별_감상_수_조회() throws Exception {

        ResultActions resultActions = mvc.perform(
                get("/api/v1/statistics/monthly-diary-graph")
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(StatisticsController.class))
                .andExpect(handler().methodName("getLast6MonthsDiaryCounts"))
                .andExpect(status().isOk());

        List<MonthlyDiaryCount> monthlyDiaryCounts = statisticsService.getLast6MonthsDiaryCountsByUser(1);

        for(int i = 0; i < monthlyDiaryCounts.size(); i++) {
            MonthlyDiaryCount monthlyDiaryCount = monthlyDiaryCounts.get(i);
            System.out.println("Period: " + monthlyDiaryCount.getPeriod() + ", Views: " + monthlyDiaryCount.getViews());
            resultActions
                    .andExpect(jsonPath("$.[%d].period".formatted(i)).value(monthlyDiaryCount.getPeriod()))
                    .andExpect(jsonPath("$.[%d].views".formatted(i)).value(monthlyDiaryCount.getViews()));
        }
    }

    @Test
    @DisplayName("콘텐츠 타입 분포 조회")
    @WithUserDetails("user1@test.com")
    void 콘텐츠_타입_분포_조회() throws Exception {

        ResultActions resultActions = mvc.perform(
                get("/api/v1/statistics/type-distribution")
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(StatisticsController.class))
                .andExpect(handler().methodName("getTypeDistribution"))
                .andExpect(status().isOk());

        List<TypeCountDto> typeCountDtos = statisticsService.getTypeDistributionByUser(1);

        for(int i = 0; i < typeCountDtos.size(); i++) {
            TypeCountDto typeCountDto = typeCountDtos.get(i);
            System.out.println("Type: " + typeCountDto.getType() + ", Count: " + typeCountDto.getCount());
            resultActions
                    .andExpect(jsonPath("$.[%d].type".formatted(i)).value(typeCountDto.getType()))
                    .andExpect(jsonPath("$.[%d].count".formatted(i)).value(typeCountDto.getCount()));
        }
    }

    @Test
    @DisplayName("타입 그래프 조회")
    @WithUserDetails("user1@test.com")
    void 타입_그래프() throws Exception {

        ResultActions resultActions = mvc.perform(
                get("/api/v1/statistics/type-graph")
                .param("period", "ALL")  // 예시로 MONTH 기간을 사용
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(StatisticsController.class))
                .andExpect(handler().methodName("getTypeGraph"))
                .andExpect(status().isOk());

        TypeGraphResponse typeGraphResponse = statisticsService.getTypeGraph(new TypeGraphRequest(1, PeriodOption.ALL));

        List<TypeLineGraphDto> trend = typeGraphResponse.getTypeLineGraph();
        List<TypeRankDto> ranking = typeGraphResponse.getTypeRanking();

        for(int i = 0; i < trend.size(); i++) {
            TypeLineGraphDto typeLineGraphDto = trend.get(i);
            System.out.println("Axis Label: " + typeLineGraphDto.getAxisLabel() + ", Type: " + typeLineGraphDto.getType() + ", Count: " + typeLineGraphDto.getCount());
            resultActions
                    .andExpect(jsonPath("$.typeLineGraph.[%d].axisLabel".formatted(i)).value(typeLineGraphDto.getAxisLabel()))
                    .andExpect(jsonPath("$.typeLineGraph.[%d].type".formatted(i)).value(typeLineGraphDto.getType().name()))
                    .andExpect(jsonPath("$.typeLineGraph.[%d].count".formatted(i)).value(typeLineGraphDto.getCount()));
        }
        for(int i = 0; i < ranking.size(); i++) {
            TypeRankDto typeRankDto = ranking.get(i);
            System.out.println("Type: " + typeRankDto.getType() + ", Count: " + typeRankDto.getTotalCount());
            resultActions
                    .andExpect(jsonPath("$.typeRanking.[%d].type".formatted(i)).value(typeRankDto.getType().name()))
                    .andExpect(jsonPath("$.typeRanking.[%d].totalCount".formatted(i)).value(typeRankDto.getTotalCount()));
        }

    }

    @Test
    @DisplayName("장르 그래프 조회")
    @WithUserDetails("user1@test.com")
    void 장르_그래프() throws Exception {

        ResultActions resultActions = mvc.perform(
                get("/api/v1/statistics/genre-graph")
                        .param("period", "ALL")  // 예시로 MONTH 기간을 사용
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(StatisticsController.class))
                .andExpect(handler().methodName("getGenreGraph"))
                .andExpect(status().isOk());

        GenreGraphResponse genreGraphResponse = statisticsService.getGenreGraph(1, PeriodOption.ALL);

        List<GenreLineGraphDto> graph = genreGraphResponse.getGenreLineGraph();
        List<GenreRankDto> ranking = genreGraphResponse.getGenreRanking();

        for(int i = 0; i < graph.size(); i++) {
            GenreLineGraphDto genreLineGraphDto = graph.get(i);
            System.out.println("Axis Label: " + genreLineGraphDto.getAxisLabel() + ", Genre: " + genreLineGraphDto.getGenre() + ", Count: " + genreLineGraphDto.getCount());
            resultActions
                    .andExpect(jsonPath("$.genreLineGraph.[%d].axisLabel".formatted(i)).value(genreLineGraphDto.getAxisLabel()))
                    .andExpect(jsonPath("$.genreLineGraph.[%d].genre".formatted(i)).value(genreLineGraphDto.getGenre()))
                    .andExpect(jsonPath("$.genreLineGraph.[%d].count".formatted(i)).value(genreLineGraphDto.getCount()));
        }

        for(int i = 0; i < ranking.size(); i++) {
            GenreRankDto genreRankDto = ranking.get(i);
            System.out.println("Genre: " + genreRankDto.getGenre() + ", Count: " + genreRankDto.getTotalCount());
            resultActions
                    .andExpect(jsonPath("$.genreRanking.[%d].genre".formatted(i)).value(genreRankDto.getGenre()))
                    .andExpect(jsonPath("$.genreRanking.[%d].totalCount".formatted(i)).value(genreRankDto.getTotalCount()));
        }
    }

    @Test
    @DisplayName("감정 그래프 조회")
    @WithUserDetails("user1@test.com")
    void 감정_그래프() throws Exception {

        ResultActions resultActions = mvc.perform(
                get("/api/v1/statistics/emotion-graph")
                        .param("period", "ALL")  // 예시로 MONTH 기간을 사용
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(StatisticsController.class))
                .andExpect(handler().methodName("getEmotionGraph"))
                .andExpect(status().isOk());

        EmotionGraphResponse emotionGraphResponse = statisticsService.getEmotionGraph(1, PeriodOption.ALL);

        List<EmotionLineGraphDto> line = emotionGraphResponse.getEmotionLineGraph();
        List<EmotionRankDto> ranking = emotionGraphResponse.getEmotionRanking();

        for(int i = 0; i < line.size(); i++) {
            EmotionLineGraphDto emotionLineGraphDto = line.get(i);
            System.out.println("Axis Label: " + emotionLineGraphDto.getAxisLabel() + ", Emotion: " + emotionLineGraphDto.getEmotion() + ", Count: " + emotionLineGraphDto.getCount());
            resultActions
                    .andExpect(jsonPath("$.emotionLineGraph.[%d].axisLabel".formatted(i)).value(emotionLineGraphDto.getAxisLabel()))
                    .andExpect(jsonPath("$.emotionLineGraph.[%d].emotion".formatted(i)).value(emotionLineGraphDto.getEmotion()))
                    .andExpect(jsonPath("$.emotionLineGraph.[%d].count".formatted(i)).value(emotionLineGraphDto.getCount()));
        }
        for(int i = 0; i < ranking.size(); i++) {
            EmotionRankDto emotionRankDto = ranking.get(i);
            System.out.println("Emotion: " + emotionRankDto.getEmotion() + ", Count: " + emotionRankDto.getTotalCount());
            resultActions
                    .andExpect(jsonPath("$.emotionRanking.[%d].emotion".formatted(i)).value(emotionRankDto.getEmotion()))
                    .andExpect(jsonPath("$.emotionRanking.[%d].totalCount".formatted(i)).value(emotionRankDto.getTotalCount()));
        }
    }

    @Test
    @DisplayName("ott 그래프 조회")
    @WithUserDetails("user1@test.com")
    void ott_그래프() throws Exception {

        ResultActions resultActions = mvc.perform(
                get("/api/v1/statistics/ott-graph")
                        .param("period", "ALL")  // 예시로 MONTH 기간을 사용
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(StatisticsController.class))
                .andExpect(handler().methodName("getOttGraph"))
                .andExpect(status().isOk());

        OttGraphResponse ottGraphResponse = statisticsService.getOttGraph(1, PeriodOption.ALL);

        List<OttLineGraphDto> line = ottGraphResponse.getOttLineGraph();
        List<OttRankDto> ranking = ottGraphResponse.getOttRanking();

        for(int i = 0; i < line.size(); i++) {
            OttLineGraphDto ottLineGraphDto = line.get(i);
            System.out.println("Axis Label: " + ottLineGraphDto.getAxisLabel() + ", OTT: " + ottLineGraphDto.getOttName() + ", Count: " + ottLineGraphDto.getCount());
            resultActions
                    .andExpect(jsonPath("$.ottLineGraph.[%d].axisLabel".formatted(i)).value(ottLineGraphDto.getAxisLabel()))
                    .andExpect(jsonPath("$.ottLineGraph.[%d].ottName".formatted(i)).value(ottLineGraphDto.getOttName()))
                    .andExpect(jsonPath("$.ottLineGraph.[%d].count".formatted(i)).value(ottLineGraphDto.getCount()));
        }

        for(int i = 0; i < ranking.size(); i++) {
            OttRankDto ottRankDto = ranking.get(i);
            System.out.println("OTT: " + ottRankDto.getOttName() + ", Count: " + ottRankDto.getTotalCount());
            resultActions
                    .andExpect(jsonPath("$.ottRanking.[%d].ottName".formatted(i)).value(ottRankDto.getOttName()))
                    .andExpect(jsonPath("$.ottRanking.[%d].totalCount".formatted(i)).value(ottRankDto.getTotalCount()));
        }
    }
}
