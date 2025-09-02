package com.back.ourlog.domain.like.controller

import com.back.ourlog.domain.like.dto.LikeCountResponse
import com.back.ourlog.domain.like.service.LikeService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(LikeController::class) // 1. LikeController만 테스트하도록 지정
class LikeControllerTest @Autowired constructor(
    private val mockMvc: MockMvc, // 3. HTTP 요청을 시뮬레이션하기 위한 MockMvc
    private val objectMapper: ObjectMapper, // 객체를 JSON으로 변환하기 위함 (선택사항)
) {

    @MockitoBean // 2. LikeService를 가짜 객체(Mock)로 대체
    private lateinit var likeService: LikeService

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/likes/{diaryId} 요청 시 좋아요가 성공하고 상태 DTO를 반환한다")
    fun `좋아요 등록 테스트`() {
        // given - 가짜 서비스가 어떻게 동작할지 정의
        val diaryId = 1
        // likeService.like(1)이 호출되면 true를 반환하도록 설정
        whenever(likeService.like(any())).thenReturn(true)
        // likeService.getLikeCount(1)이 호출되면 10을 반환하도록 설정
        whenever(likeService.getLikeCount(any())).thenReturn(10)

        // when - 실제 HTTP 요청을 시뮬레이션
        val resultActions = mockMvc.perform(
            post("/api/v1/likes/{diaryId}", diaryId)
                .with(csrf())
        )

        // then - 응답 결과를 검증
        resultActions
            .andExpect(status().isOk) // HTTP 상태가 200 OK인지 확인
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200")) // JSON 응답의 resultCode 확인
            .andExpect(jsonPath("$.data.liked").value(true)) // JSON 응답의 data.liked 값 확인
            .andExpect(jsonPath("$.data.likeCount").value(10)) // JSON 응답의 data.likeCount 값 확인
            .andDo(print()) // 요청/응답 전체 내용 출력
    }

    @Test
    @WithMockUser // 1. 인증된 사용자로 테스트 실행
    @DisplayName("DELETE /api/v1/likes/{diaryId} 요청 시 좋아요가 취소되고 상태 DTO를 반환한다")
    fun `좋아요 취소 테스트`() {
        // given - 가짜 서비스가 어떻게 동작할지 정의
        val diaryId = 1
        // unlike 메서드는 반환값이 없으므로 별도의 whenever 설정은 필요 없습니다.
        // getLikeCount는 5를 반환하도록 설정 (like 테스트와 다른 값으로 설정)
        whenever(likeService.getLikeCount(any())).thenReturn(5)

        // when - 실제 HTTP DELETE 요청을 시뮬레이션
        val resultActions = mockMvc.perform(
            delete("/api/v1/likes/{diaryId}", diaryId) // 2. post -> delete로 변경
                .with(csrf()) // 3. 데이터 변경 요청이므로 CSRF 토큰 포함
        )

        // then - 응답 결과를 검증
        resultActions
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.data.liked").value(false)) // 4. liked는 false여야 함
            .andExpect(jsonPath("$.data.likeCount").value(5)) // 4. getLikeCount에서 반환하기로 한 값
            .andDo(print())
    }

    @Test
    @WithMockUser // 1. 인증된 사용자로 테스트 실행
    @DisplayName("GET /api/v1/likes/count 요청 시 좋아요 개수 DTO를 반환한다")
    fun `좋아요 개수 조회 테스트`() {
        // given
        val diaryId = 1
        val expectedCount = 7
        // getLikeCount가 호출되면 LikeCountResponse DTO를 반환하도록 설정
        whenever(likeService.getLikeCount(diaryId)).thenReturn(expectedCount)

        // when - 실제 HTTP GET 요청을 시뮬레이션
        val resultActions = mockMvc.perform(
            get("/api/v1/likes/count") // 2. GET 요청으로 변경
                .param("diaryId", diaryId.toString()) // 3. @RequestParam을 .param()으로 추가
        )

        // then - 응답 결과를 검증
        resultActions
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.data").value(expectedCount)) // 4. 예상된 개수 검증
            .andDo(print())
    }

}