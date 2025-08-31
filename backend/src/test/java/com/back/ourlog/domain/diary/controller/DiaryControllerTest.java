package com.back.ourlog.domain.diary.controller;

import com.back.ourlog.domain.content.entity.ContentType;
import com.back.ourlog.domain.diary.dto.DiaryWriteRequestDto;
import com.back.ourlog.domain.diary.entity.Diary;
import com.back.ourlog.domain.diary.repository.DiaryRepository;
import com.back.ourlog.domain.diary.service.DiaryService;
import com.back.ourlog.domain.ott.repository.OttRepository;
import com.back.ourlog.domain.tag.repository.TagRepository;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.domain.user.repository.UserRepository;
import com.back.ourlog.global.config.RedisConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(RedisConfig.class)
@Transactional
class DiaryControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private OttRepository ottRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 저장
        testUser = userRepository.findByEmail("user1@test.com")
                .orElseGet(() -> userRepository.save(
                        User.createNormalUser(
                                "user1@test.com",
                                passwordEncoder.encode("1234"),
                                "테스트유저",
                                null,
                                null
                        )
                ));
    }

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("감상일기 등록 성공")
    void t1() throws Exception {
        String body = """
        {
            "title": "인셉션",
            "contentText": "정말 재밌었어요!",
            "rating": 4.8,
            "isPublic": true,
            "externalId": "tt1375666",
            "type": "MOVIE",
            "tagNames": ["감동"]
        }
        """;

        mvc.perform(post("/api/v1/diaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("인셉션"));
    }

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("감상일기 등록 실패 - 제목 없음")
    void t2() throws Exception {
        String body = """
    {
        "title": "",
        "contentText": "내용 있음",
        "rating": 4.0,
        "isPublic": true,
        "externalId": "tt1375666",
        "type": "MOVIE",
        "tagNames": ["슬픔"]
    }
    """;

        mvc.perform(post("/api/v1/diaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("제목을 입력해주세요."));
    }

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("감상일기 등록 실패 - 내용 없음")
    void t3() throws Exception {
        String body = """
    {
        "title": "제목 있음",
        "contentText": "",
        "rating": 3.0,
        "isPublic": true,
        "type": "BOOK",
        "externalId": "library-9791190908207",
        "tagNames": ["분노"]
    }
    """;

        mvc.perform(post("/api/v1/diaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("내용을 입력해주세요."))
                .andExpect(jsonPath("$.resultCode").value("COMMON_400"));
    }

//    @Test
//    @WithUserDetails("user1@test.com")
//    @DisplayName("감상일기 수정 성공")
//    void t4() throws Exception {
//        // 초기 등록
//        String createBody = """
//    {
//        "title": "인셉션",
//        "contentText": "원본 내용",
//        "rating": 3.5,
//        "isPublic": true,
//        "externalId": "tt1375666",
//        "type": "MOVIE",
//        "tagNames": ["감동"]
//    }
//    """;
//
//        mvc.perform(post("/api/v1/diaries")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(createBody))
//                .andExpect(status().isCreated());
//
//        Diary diary = diaryRepository.findTopByOrderByIdDesc().orElseThrow();
//        int id = diary.getId();
//
//        // 수정 요청
//        String updateBody = """
//    {
//        "title": "메멘토",
//        "contentText": "수정된 내용입니다.",
//        "rating": 4.0,
//        "isPublic": true,
//        "externalId": "tt1375666",
//        "type": "MOVIE",
//        "tagNames": ["감동", "분노"],
//        "ottIds": [1]
//    }
//    """;
//
//        mvc.perform(put("/api/v1/diaries/" + id)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(updateBody))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.title").value("메멘토"))
//                .andExpect(jsonPath("$.data.contentText").value("수정된 내용입니다."))
//                .andExpect(jsonPath("$.data.rating").value(4.0));
//    }

//    @Test
//    @WithUserDetails("user1@test.com")
//    @DisplayName("감상일기 수정 성공 - 존재하지 않는 태그지만 자동 생성됨")
//    void t5() throws Exception {
//        String createBody = """
//    {
//        "title": "테스트",
//        "contentText": "초기 내용",
//        "rating": 4.0,
//        "isPublic": true,
//        "externalId": "tt1375666",
//        "type": "MOVIE",
//        "tagNames": ["기쁨"]
//    }
//    """;
//
//        mvc.perform(post("/api/v1/diaries")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(createBody))
//                .andExpect(status().isCreated());
//
//        int id = diaryRepository.findTopByOrderByIdDesc().orElseThrow().getId();
//
//        String updateBody = """
//    {
//        "title": "테스트2",
//        "contentText": "새 내용",
//        "rating": 5.0,
//        "isPublic": true,
//        "externalId": "tt1375666",
//        "type": "MOVIE",
//        "tagNames": ["새로운감정태그"],
//        "ottIds": [1]
//    }
//    """;
//
//        mvc.perform(put("/api/v1/diaries/" + id)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(updateBody))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.title").value("테스트2"));
//    }
//
//    @Test
//    @WithUserDetails("user1@test.com")
//    @DisplayName("감상일기 수정 실패 - 존재하지 않는 OTT ID")
//    void t6() throws Exception {
//        String createBody = """
//    {
//        "title": "테스트",
//        "contentText": "초기 내용",
//        "rating": 4.0,
//        "isPublic": true,
//        "externalId": "tt1375666",
//        "type": "MOVIE",
//        "tagNames": ["놀람"]
//    }
//    """;
//
//        mvc.perform(post("/api/v1/diaries")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(createBody))
//                .andExpect(status().isCreated());
//
//        int id = diaryRepository.findTopByOrderByIdDesc().orElseThrow().getId();
//
//        String updateBody = """
//    {
//        "title": "업데이트됨",
//        "contentText": "내용 수정",
//        "rating": 4.2,
//        "isPublic": true,
//        "externalId": "tt1375666",
//        "type": "MOVIE",
//        "tagNames": ["놀람"],
//        "ottIds": [999]
//    }
//    """;
//
//        mvc.perform(put("/api/v1/diaries/" + id)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(updateBody))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.resultCode").value("OTT_001"))
//                .andExpect(jsonPath("$.msg").value("존재하지 않는 OTT입니다."));
//    }

//    @Test
//    @WithUserDetails("user1@test.com")
//    @DisplayName("감상일기 삭제 성공")
//    void t7() throws Exception {
//        DiaryWriteRequestDto dto = new DiaryWriteRequestDto(
//                "삭제 테스트",
//                "삭제 테스트 내용",
//                true,
//                4.0F,
//                ContentType.MOVIE,
//                "tt1375666",
//                List.of("감동"),
//                List.of(),
//                List.of()
//        );
//
//        Diary diary = diaryService.writeWithContentSearch(dto, testUser);
//
//        mvc.perform(delete("/api/v1/diaries/" + diary.getId()))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.msg").value("일기 삭제 완료"))
//                .andExpect(jsonPath("$.resultCode").value("200-0"));
//    }
//
//    @Test
//    @WithUserDetails("user1@test.com")
//    @DisplayName("감상일기 삭제 실패 - 존재하지 않는 ID")
//    void t8() throws Exception {
//        int id = 9999;
//        mvc.perform(delete("/api/v1/diaries/" + id))
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.resultCode").value("DIARY_001"))
//                .andExpect(jsonPath("$.msg").value("존재하지 않는 다이어리입니다."));
//    }

    @Test
    @DisplayName("감상일기 조회 성공")
    @WithUserDetails("user1@test.com")
    @Transactional(readOnly = true)
    void t9() throws Exception {
        // 테스트 유저 불러오기
        User user = userRepository.findByEmail("user1@test.com")
                .orElseThrow(() -> new RuntimeException("테스트 유저 없음"));

        // 다이어리 등록
        DiaryWriteRequestDto dto = new DiaryWriteRequestDto(
                "다이어리 조회 테스트",
                "이것은 다이어리 조회 테스트 내용입니다.",
                true,
                3.0F,
                ContentType.MOVIE,
                "tt1375666",
                List.of("감동"),
                List.of(),
                List.of()
        );

        Diary diary = diaryService.writeWithContentSearch(dto, user);

        // 등록한 다이어리 조회 요청
        mvc.perform(get("/api/v1/diaries/" + diary.getId()))
                .andDo(print())
                .andExpect(handler().handlerType(DiaryController.class))
                .andExpect(handler().methodName("getDiary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("다이어리 조회 테스트"))
                .andExpect(jsonPath("$.data.rating").value(3.0))
                .andExpect(jsonPath("$.data.contentText").value("이것은 다이어리 조회 테스트 내용입니다."))
                .andExpect(jsonPath("$.data.tagNames[0]").isNotEmpty());
    }

    @Test
    @DisplayName("감성일기 조회 실패")
    @Transactional(readOnly = true)
    void t10() throws Exception {
        int id = 100000;
        ResultActions resultActions = mvc.perform(
                get("/api/v1/diaries/" + id)
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(DiaryController.class))
                .andExpect(handler().methodName("getDiary"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("DIARY_001"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 다이어리입니다."));
    }
}