package com.back.ourlog.domain.content.controller;

import com.back.ourlog.domain.content.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ContentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("컨텐츠 조회")
    void t1() throws Exception {
        int diaryId = 1;

        ResultActions resultActions = mvc.perform(
            get("/api/v1/contents/" + diaryId)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ContentController.class))
                .andExpect(handler().methodName("getContent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("%d번 다이어리의 조회 컨텐츠가 조회되었습니다.".formatted(diaryId)))
                .andExpect(jsonPath("$.data.title").value("콘텐츠 30"));
    }

    /*
    @Test
    @DisplayName("컨텐츠 검색 - BOOK")
    void t2() throws Exception {
        mvc.perform(get("/api/v1/contents/search")
                        .param("type", ContentType.BOOK.name())
                        .param("title", "파과"))
                .andDo(print())
                .andExpect(handler().handlerType(ContentController.class))
                .andExpect(handler().methodName("searchContents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("콘텐츠 검색 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(lessThanOrEqualTo(10)))
                .andExpect(jsonPath("$.data[0].type").value("BOOK"));
    }

     */

    @Test
    @DisplayName("컨텐츠 검색 - MOVIE")
    void t3() throws Exception {
        mvc.perform(get("/api/v1/contents/search")
                        .param("type", ContentType.MOVIE.name())
                        .param("title", "미션"))
                .andDo(print())
                .andExpect(handler().handlerType(ContentController.class))
                .andExpect(handler().methodName("searchContents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("콘텐츠 검색 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(lessThanOrEqualTo(10)))
                .andExpect(jsonPath("$.data[0].type").value("MOVIE"));
    }

    @Test
    @DisplayName("컨텐츠 검색 - MUSIC")
    void t4() throws Exception {
        mvc.perform(get("/api/v1/contents/search")
                        .param("type", ContentType.MUSIC.name())
                        .param("title", "Always"))
                .andDo(print())
                .andExpect(handler().handlerType(ContentController.class))
                .andExpect(handler().methodName("searchContents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("콘텐츠 검색 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(lessThanOrEqualTo(10)))
                .andExpect(jsonPath("$.data[0].type").value("MUSIC"));
    }

    @Test
    @DisplayName("컨텐츠 검색 - 필수 파라미터 누락 시 실패")
    void t5() throws Exception {
        mvc.perform(get("/api/v1/contents/search")
                        .param("type", "BOOK")) // title 누락
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("필수 요청 파라미터 'title'가 누락되었습니다."));
    }

}