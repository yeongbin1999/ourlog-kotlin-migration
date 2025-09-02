package com.back.ourlog.domain.timeline.controller

import com.back.ourlog.domain.timeline.dto.TimelineResponse
import com.back.ourlog.domain.timeline.service.TimelineService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

// 1. @WebMvcTest: TimelineController와 웹 계층 관련 설정만 로드하여 가볍게 테스트합니다.
@WebMvcTest(
    controllers = [TimelineController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
class TimelineControllerTest {

    // 2. MockMvc: 컨트롤러에 가짜 HTTP 요청을 보낼 수 있는 핵심 객체입니다.
    @Autowired
    private lateinit var mockMvc: MockMvc

    // 3. @MockBean: 실제 TimelineService 대신 가짜 객체를 Spring Context에 주입합니다.
    // 이 테스트에서 timelineService는 실제 로직을 수행하지 않습니다.
    @MockitoBean
    private lateinit var timelineService: TimelineService

    @Test
    @DisplayName("GET /api/v1/timeline 요청 시, 성공 응답(200 OK)과 타임라인 데이터를 반환한다")
    fun `타임라인 조회 API는 성공 시 200 OK와 데이터를 반환한다`() {
        // given (준비)
        // 4. Service가 반환할 가짜 데이터를 미리 정의합니다.
        val fakeResponse = listOf(
            TimelineResponse(
                id = 1,
                title = "테스트 일기",
                content = "테스트 내용입니다.",
                createdAt = LocalDateTime.now().toString(),
                imageUrl = "http://image.url/1",
                likeCount = 10,
                commentCount = 5,
                isLiked = true,
                user = TimelineResponse.UserSummary(101, "테스트 유저", "http://profile.url/1")
            )
        )
        // 5. timelineService.getPublicTimeline()이 호출되면, 위에서 만든 가짜 데이터를 반환하도록 설정합니다.
        given(timelineService.getPublicTimeline()).willReturn(fakeResponse)

        // when (실행) & then (검증)
        // 6. GET /api/v1/timeline 으로 가짜 HTTP 요청을 보내고, 그 결과를 검증합니다.
        mockMvc.perform(
            get("/api/v1/timeline")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk) // 7. HTTP 상태 코드가 200 OK 인지 검증
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200")) // 8. 응답 JSON의 공통 포맷(RsData) 검증
            .andExpect(jsonPath("$.msg").value("타임라인 조회 성공"))
            .andExpect(jsonPath("$.data[0].id").value(1)) // 9. 응답 JSON의 데이터 상세 내용 검증
            .andExpect(jsonPath("$.data[0].title").value("테스트 일기"))
            .andExpect(jsonPath("$.data[0].likeCount").value(10))
            .andExpect(jsonPath("$.data[0].user.nickname").value("테스트 유저"))
    }
}
