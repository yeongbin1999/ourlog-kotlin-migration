package com.back.ourlog.domain.comment.controller;

import com.back.ourlog.domain.comment.entity.Comment;
import com.back.ourlog.domain.comment.repository.CommentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class CommentControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CommentRepository commentRepository;
    @Test
    @DisplayName("댓글 작성")
    @WithUserDetails("user1@test.com")
    void t1() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("diaryId", 1);
        data.put("content", "안녕하시렵니까?");

        String json = objectMapper.writeValueAsString(data);

        ResultActions resultActions = mvc.perform(
                post("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        // 헤더 부분에 AccessToken 추가
                        .content(json)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("writeComment"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content").value("안녕하시렵니까?"))
                .andExpect(jsonPath("$.data.userId").value(1));
    }

    @Test
    @DisplayName("댓글 작성 - 댓글 내용이 없음")
    @WithUserDetails("user1@test.com")
    void t2() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("diaryId", 1);
        data.put("content", "");

        String json = objectMapper.writeValueAsString(data);

        ResultActions resultActions = mvc.perform(
                post("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("writeComment"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("COMMON_400"))
                .andExpect(jsonPath("$.msg").value("must not be blank"));
    }

    @Test
    @DisplayName("댓글 조회")
    void t3() throws Exception {
        int diaryId = 1;

        ResultActions resultActions = mvc.perform(
                get("/api/v1/comments/" + diaryId)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("getComments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @DisplayName("댓글 조회 - 존재하지 않는 diaryId")
    void t4() throws Exception {
        int diaryId = 99999;

        ResultActions resultActions = mvc.perform(
                get("/api/v1/comments/" + diaryId)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("getComments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("DIARY_001"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 다이어리입니다."));
    }

    @Test
    @DisplayName("댓글 수정")
    @WithUserDetails("user1@test.com")
    void t5() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("id", 1);
        data.put("content", "안녕하시렵니까?");

        String json = objectMapper.writeValueAsString(data);

        ResultActions resultActions = mvc.perform(
                put("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("updateComment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-0"))
                .andExpect(jsonPath("$.msg").value("1번 댓글이 수정되었습니다."));

        // 실제 1번 댓글의 content 가 변했는지 확인
        Comment comment = commentRepository.findById(1).get();
        assertThat(comment.getContent()).isEqualTo("안녕하시렵니까?");
    }

    @Test
    @DisplayName("댓글 수정 - 존재하지 않는 댓글 ID")
    @WithUserDetails("user1@test.com")
    void t6() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("id", 1000000);
        data.put("content", "안녕하시렵니까?");

        String json = objectMapper.writeValueAsString(data);

        ResultActions resultActions = mvc.perform(
                put("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("updateComment"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("COMMENT_001"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 댓글입니다."));
    }

    @Test
    @DisplayName("댓글 삭제")
    @WithUserDetails("user1@test.com")
    void t7() throws Exception {
        int id = 1;

        ResultActions resultActions = mvc.perform(
                delete("/api/v1/comments/" + id)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("deleteComment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-0"))
                .andExpect(jsonPath("$.msg").value("1번 댓글이 삭제되었습니다."));

        // 1번 댓글이 실제로 사라졌는지 확인
        Comment comment = commentRepository.findById(1).orElse(null);
        assertThat(comment).isNull();
    }

    @Test
    @DisplayName("댓글 삭제 - 존재하지 않는 댓글 ID")
    @WithUserDetails("user1@test.com")
    void t8() throws Exception {
        int id = 1000000;

        ResultActions resultActions = mvc.perform(
                delete("/api/v1/comments/" + id)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(CommentController.class))
                .andExpect(handler().methodName("deleteComment"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("COMMENT_001"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 댓글입니다."));
    }
}