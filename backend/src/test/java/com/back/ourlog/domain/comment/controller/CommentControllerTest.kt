package com.back.ourlog.domain.comment.controller

import com.back.ourlog.domain.comment.repository.CommentRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class CommentControllerTest @Autowired constructor(
    private val mvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val commentRepository: CommentRepository,
) {
    @Test
    @DisplayName("댓글 작성")
    @WithUserDetails("user1@test.com")
    fun t1() {
        val data = mapOf(
            "diaryId" to 1,
            "content" to "안녕하시렵니까?"
        )

        val json = objectMapper.writeValueAsString(data)

        mvc.perform(
            post("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andDo(print())
            .andExpect(handler().handlerType(CommentController::class.java))
            .andExpect(handler().methodName("writeComment"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").value("안녕하시렵니까?"))
            .andExpect(jsonPath("$.data.userId").value(1))
    }

    @Test
    @DisplayName("댓글 작성 - 댓글 내용이 없음")
    @WithUserDetails("user1@test.com")
    fun t2() {
        val data = mapOf(
            "diaryId" to 1,
            "content" to ""
        )

        val json = objectMapper.writeValueAsString(data)

        mvc.perform(
            post("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andDo(print())
            .andExpect(handler().handlerType(CommentController::class.java))
            .andExpect(handler().methodName("writeComment"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode").value("COMMON_400"))
            .andExpect(jsonPath("$.msg").value("must not be blank"))
    }

    @Test
    @DisplayName("댓글 조회")
    fun t3() {
        val diaryId = 1

        mvc.perform(
            get("/api/v1/comments/$diaryId")
        ).andDo(print())
            .andExpect(handler().handlerType(CommentController::class.java))
            .andExpect(handler().methodName("getComments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isNotEmpty())
    }

    @Test
    @DisplayName("댓글 조회 - 존재하지 않는 diaryId")
    fun t4() {
        val diaryId = 99999

        mvc.perform(
            get("/api/v1/comments/$diaryId")
        ).andDo(print())
            .andExpect(handler().handlerType(CommentController::class.java))
            .andExpect(handler().methodName("getComments"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("DIARY_001"))
            .andExpect(jsonPath("$.msg").value("존재하지 않는 다이어리입니다."))
    }

    @Test
    @DisplayName("댓글 수정")
    @WithUserDetails("user1@test.com")
    fun t5() {
        val data = mapOf(
            "id" to 1,
            "content" to "안녕하시렵니까?"
        )

        val json = objectMapper.writeValueAsString(data)

        mvc.perform(
            put("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andDo(print())
            .andExpect(handler().handlerType(CommentController::class.java))
            .andExpect(handler().methodName("updateComment"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.msg").value("1번 댓글이 수정되었습니다."))

        // 실제 1번 댓글의 content 가 변했는지 확인
        val comment = commentRepository.findByIdOrNull(1)
        assertThat(comment!!.content).isEqualTo("안녕하시렵니까?")
    }

    @Test
    @DisplayName("댓글 수정 - 존재하지 않는 댓글 ID")
    @WithUserDetails("user1@test.com")
    fun t6() {
        val data = mapOf(
            "id" to 100000,
            "content" to "안녕하시렵니까?"
        )

        val json = objectMapper.writeValueAsString(data)

        mvc.perform(
            put("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andDo(print())
            .andExpect(handler().handlerType(CommentController::class.java))
            .andExpect(handler().methodName("updateComment"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("COMMENT_001"))
            .andExpect(jsonPath("$.msg").value("존재하지 않는 댓글입니다."))
    }

    @Test
    @DisplayName("댓글 삭제")
    @WithUserDetails("user1@test.com")
    fun t7() {
        val id = 1

        mvc.perform(
            delete("/api/v1/comments/$id")
        ).andDo(print())
            .andExpect(handler().handlerType(CommentController::class.java))
            .andExpect(handler().methodName("deleteComment"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.msg").value("1번 댓글이 삭제되었습니다."))
    }

    @Test
    @DisplayName("댓글 삭제 - 존재하지 않는 댓글 ID")
    @WithUserDetails("user1@test.com")
    fun t8() {
        val id = 1000000

        mvc.perform(
            delete("/api/v1/comments/$id")
        ).andDo(print())
            .andExpect(handler().handlerType(CommentController::class.java))
            .andExpect(handler().methodName("deleteComment"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode").value("COMMENT_001"))
            .andExpect(jsonPath("$.msg").value("존재하지 않는 댓글입니다."))
    }
}