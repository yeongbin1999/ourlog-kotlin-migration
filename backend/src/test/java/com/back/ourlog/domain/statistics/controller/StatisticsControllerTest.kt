package com.back.ourlog.domain.statistics.controller

import com.back.ourlog.domain.statistics.enums.PeriodOption
import com.back.ourlog.domain.statistics.service.StatisticsService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class StatisticsControllerTest @Autowired constructor(
    private val mvc: MockMvc,
    private val statisticsService: StatisticsService
) {

    @Test
    @DisplayName("통계 카드 조회")
    @WithUserDetails("user1@test.com")
    fun `통계 카드 조회`() {
        val resultActions = mvc.get("/api/v1/statistics/card").andDo { print() }

        resultActions
            .andExpect { MockMvcResultMatchers.handler().handlerType(StatisticsController::class.java) }
            .andExpect { MockMvcResultMatchers.handler().methodName("getStatisticsCard") }
            .andExpect { MockMvcResultMatchers.status().isOk }

        val statisticsCardDto = statisticsService.getStatisticsCardByUserId(1)

        resultActions
            .andExpect { MockMvcResultMatchers.jsonPath("$.totalDiaryCount").value(statisticsCardDto.totalDiaryCount) }
            .andExpect { MockMvcResultMatchers.jsonPath("$.averageRating").value(statisticsCardDto.averageRating) }
            .andExpect { MockMvcResultMatchers.jsonPath("$.favoriteTypeAndCountDto.favoriteType").value(statisticsCardDto.favoriteTypeAndCountDto.favoriteType) }
            .andExpect { MockMvcResultMatchers.jsonPath("$.favoriteTypeAndCountDto.favoriteTypeCount").value(statisticsCardDto.favoriteTypeAndCountDto.favoriteTypeCount) }
            .andExpect { MockMvcResultMatchers.jsonPath("$.favoriteEmotionAndCountDto.favoriteEmotion").value(statisticsCardDto.favoriteEmotionAndCountDto.favoriteEmotion)}
            .andExpect { MockMvcResultMatchers.jsonPath("$.favoriteEmotionAndCountDto.favoriteEmotionCount").value(statisticsCardDto.favoriteEmotionAndCountDto.favoriteEmotionCount) }

        println("Total Diary Count: ${statisticsCardDto.totalDiaryCount}")
        println("Average Rating: ${statisticsCardDto.averageRating}")
        println("Favorite Type: ${statisticsCardDto.favoriteTypeAndCountDto.favoriteType}")
        println("Favorite Type Count: ${statisticsCardDto.favoriteTypeAndCountDto.favoriteTypeCount}")
        println("Favorite Emotion: ${statisticsCardDto.favoriteEmotionAndCountDto.favoriteEmotion}")
        println("Favorite Emotion Count: ${statisticsCardDto.favoriteEmotionAndCountDto.favoriteEmotionCount}")
    }

    @Test
    @DisplayName("최근 6개월 월 별 감상 수 조회")
    @WithUserDetails("user1@test.com")
    fun `최근 6개월 월 별 감상 수 조회`() {
        val resultActions = mvc.get("/api/v1/statistics/monthly-diary-graph").andDo { print() }

        resultActions
            .andExpect { MockMvcResultMatchers.handler().handlerType(StatisticsController::class.java) }
            .andExpect { MockMvcResultMatchers.handler().methodName("getLast6MonthsDiaryCounts") }
            .andExpect { MockMvcResultMatchers.status().isOk }

        val monthlyDiaryCounts = statisticsService.getLast6MonthsDiaryCountsByUser(1)

        monthlyDiaryCounts.forEachIndexed { i, monthlyDiaryCount ->
            println("Period: ${monthlyDiaryCount.period}, Views: ${monthlyDiaryCount.views}")
            resultActions
                .andExpect { MockMvcResultMatchers.jsonPath("$.[$i].period").value(monthlyDiaryCount.period) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.[$i].views").value(monthlyDiaryCount.views) }
        }
    }

    @Test
    @DisplayName("콘텐츠 타입 분포 조회")
    @WithUserDetails("user1@test.com")
    fun `콘텐츠 타입 분포 조회`() {
        val resultActions = mvc.get("/api/v1/statistics/type-distribution").andDo { print() }

        resultActions
            .andExpect { MockMvcResultMatchers.handler().handlerType(StatisticsController::class.java) }
            .andExpect { MockMvcResultMatchers.handler().methodName("getTypeDistribution") }
            .andExpect { MockMvcResultMatchers.status().isOk }

        val typeCountDtos = statisticsService.getTypeDistributionByUser(1)

        typeCountDtos.forEachIndexed { i, typeCountDto ->
            println("Type: ${typeCountDto.type}, Count: ${typeCountDto.count}")
            resultActions
                .andExpect { MockMvcResultMatchers.jsonPath("$.[$i].type").value(typeCountDto.type) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.[$i].count").value(typeCountDto.count) }
        }
    }

    @Test
    @DisplayName("타입 그래프 조회")
    @WithUserDetails("user1@test.com")
    fun `타입 그래프`() {
        val resultActions = mvc.get("/api/v1/statistics/type-graph") {
            param("period", "ALL")
        }.andDo { print() }

        resultActions
            .andExpect { MockMvcResultMatchers.handler().handlerType(StatisticsController::class.java) }
            .andExpect { MockMvcResultMatchers.handler().methodName("getTypeGraph") }
            .andExpect { MockMvcResultMatchers.status().isOk }

        val typeGraphResponse = statisticsService.getTypeGraph(1, PeriodOption.ALL)

        val trend = typeGraphResponse.typeLineGraph
        val ranking = typeGraphResponse.typeRanking

        trend.forEachIndexed { i, typeLineGraphDto ->
            println("Axis Label: ${typeLineGraphDto.axisLabel}, Type: ${typeLineGraphDto.type}, Count: ${typeLineGraphDto.count}")
            resultActions
                .andExpect { MockMvcResultMatchers.jsonPath("$.typeLineGraph.[$i].axisLabel").value(typeLineGraphDto.axisLabel) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.typeLineGraph.[$i].type").value(typeLineGraphDto.type.name) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.typeLineGraph.[$i].count").value(typeLineGraphDto.count) }
        }
        ranking.forEachIndexed { i, typeRankDto ->
            println("Type: ${typeRankDto.type}, Count: ${typeRankDto.totalCount}")
            resultActions
                .andExpect { MockMvcResultMatchers.jsonPath("$.typeRanking.[$i].type").value(typeRankDto.type.name) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.typeRanking.[$i].totalCount").value(typeRankDto.totalCount) }
        }
    }

    @Test
    @DisplayName("장르 그래프 조회")
    @WithUserDetails("user1@test.com")
    fun `장르 그래프`() {
        val resultActions = mvc.get("/api/v1/statistics/genre-graph") {
            param("period", "ALL")
        }.andDo { print() }

        resultActions
            .andExpect { MockMvcResultMatchers.handler().handlerType(StatisticsController::class.java) }
            .andExpect { MockMvcResultMatchers.handler().methodName("getGenreGraph") }
            .andExpect { MockMvcResultMatchers.status().isOk }

        val genreGraphResponse = statisticsService.getGenreGraph(1, PeriodOption.ALL)

        val graph = genreGraphResponse.genreLineGraph
        val ranking = genreGraphResponse.genreRanking

        graph.forEachIndexed { i, genreLineGraphDto ->
            println("Axis Label: ${genreLineGraphDto.axisLabel}, Genre: ${genreLineGraphDto.genre}, Count: ${genreLineGraphDto.count}")
            resultActions
                .andExpect { MockMvcResultMatchers.jsonPath("$.genreLineGraph.[$i].axisLabel").value(genreLineGraphDto.axisLabel) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.genreLineGraph.[$i].genre").value(genreLineGraphDto.genre) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.genreLineGraph.[$i].count").value(genreLineGraphDto.count) }
        }

        ranking.forEachIndexed { i, genreRankDto ->
            println("Genre: ${genreRankDto.genre}, Count: ${genreRankDto.totalCount}")
            resultActions
                .andExpect { MockMvcResultMatchers.jsonPath("$.genreRanking.[$i].genre").value(genreRankDto.genre) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.genreRanking.[$i].totalCount").value(genreRankDto.totalCount) }
        }
    }

    @Test
    @DisplayName("감정 그래프 조회")
    @WithUserDetails("user1@test.com")
    fun `감정 그래프`() {
        val resultActions = mvc.get("/api/v1/statistics/emotion-graph") {
            param("period", "ALL")
        }.andDo { print() }

        resultActions
            .andExpect { MockMvcResultMatchers.handler().handlerType(StatisticsController::class.java) }
            .andExpect { MockMvcResultMatchers.handler().methodName("getEmotionGraph") }
            .andExpect { MockMvcResultMatchers.status().isOk }

        val emotionGraphResponse = statisticsService.getEmotionGraph(1, PeriodOption.ALL)

        val line = emotionGraphResponse.emotionLineGraph
        val ranking = emotionGraphResponse.emotionRanking

        line.forEachIndexed { i, emotionLineGraphDto ->
            println("Axis Label: ${emotionLineGraphDto.axisLabel}, Emotion: ${emotionLineGraphDto.emotion}, Count: ${emotionLineGraphDto.count}")
            resultActions
                .andExpect { MockMvcResultMatchers.jsonPath("$.emotionLineGraph.[$i].axisLabel").value(emotionLineGraphDto.axisLabel) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.emotionLineGraph.[$i].emotion").value(emotionLineGraphDto.emotion) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.emotionLineGraph.[$i].count").value(emotionLineGraphDto.count) }
        }
        ranking.forEachIndexed { i, emotionRankDto ->
            println("Emotion: ${emotionRankDto.emotion}, Count: ${emotionRankDto.totalCount}")
            resultActions
                .andExpect { MockMvcResultMatchers.jsonPath("$.emotionRanking.[$i].emotion").value(emotionRankDto.emotion) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.emotionRanking.[$i].totalCount").value(emotionRankDto.totalCount) }
        }
    }

    @Test
    @DisplayName("ott 그래프 조회")
    @WithUserDetails("user1@test.com")
    fun `ott 그래프`() {
        val resultActions = mvc.get("/api/v1/statistics/ott-graph") {
            param("period", "ALL")
        }.andDo { print() }

        resultActions
            .andExpect { MockMvcResultMatchers.handler().handlerType(StatisticsController::class.java) }
            .andExpect { MockMvcResultMatchers.handler().methodName("getOttGraph") }
            .andExpect { MockMvcResultMatchers.status().isOk }

        val ottGraphResponse = statisticsService.getOttGraph(1, PeriodOption.ALL)

        val line = ottGraphResponse.ottLineGraph
        val ranking = ottGraphResponse.ottRanking

        line.forEachIndexed { i, ottLineGraphDto ->
            println("Axis Label: ${ottLineGraphDto.axisLabel}, OTT: ${ottLineGraphDto.ottName}, Count: ${ottLineGraphDto.count}")
            resultActions
                .andExpect { MockMvcResultMatchers.jsonPath("$.ottLineGraph.[$i].axisLabel").value(ottLineGraphDto.axisLabel) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.ottLineGraph.[$i].ottName").value(ottLineGraphDto.ottName) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.ottLineGraph.[$i].count").value(ottLineGraphDto.count) }
        }

        ranking.forEachIndexed { i, ottRankDto ->
            println("OTT: ${ottRankDto.ottName}, Count: ${ottRankDto.totalCount}")
            resultActions
                .andExpect { MockMvcResultMatchers.jsonPath("$.ottRanking.[$i].ottName").value(ottRankDto.ottName) }
                .andExpect { MockMvcResultMatchers.jsonPath("$.ottRanking.[$i].totalCount").value(ottRankDto.totalCount) }
        }
    }
}