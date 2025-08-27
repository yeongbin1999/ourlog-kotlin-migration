package com.back.ourlog.domain.diary.service;

import com.back.ourlog.domain.content.entity.ContentType;
import com.back.ourlog.domain.diary.dto.DiaryWriteRequestDto;
import com.back.ourlog.domain.diary.entity.Diary;
import com.back.ourlog.domain.diary.repository.DiaryRepository;
import com.back.ourlog.domain.ott.entity.Ott;
import com.back.ourlog.domain.ott.repository.OttRepository;
import com.back.ourlog.domain.tag.entity.Tag;
import com.back.ourlog.domain.tag.repository.TagRepository;
import com.back.ourlog.global.config.RedisConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import(RedisConfig.class)
@Transactional
class DiaryServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private OttRepository ottRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("감상일기 등록 → DiaryTag 생성")
    void t1() throws Exception {
        DiaryWriteRequestDto requestDto = new DiaryWriteRequestDto(
                "인셉션",
                "테스트 내용",
                true,
                4.0F,
                ContentType.MOVIE,
                "tt1375666",
                List.of("감동", "분노"),
                List.of(), // genreNames
                List.of()  // ottIds
        );

        mockMvc.perform(post("/api/v1/diaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        Diary savedDiary = diaryRepository.findTopByOrderByIdDesc().orElseThrow();

        assertThat(savedDiary.getDiaryTags()).hasSize(2);
        assertThat(savedDiary.getDiaryTags())
                .extracting(dt -> dt.getTag().getName())
                .containsExactlyInAnyOrder("감동", "분노");
    }

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("감상일기 등록 → 외부 API 기반 DiaryGenre 자동 생성")
    void t2() throws Exception {
        tagRepository.save(new Tag("더미"));

        String body = """
    {
        "title": "인셉션",
        "contentText": "장르 테스트",
        "rating": 4.0,
        "isPublic": true,
        "externalId": "tt1375666",
        "type": "MOVIE",
        "tagNames": ["더미"]
    }
""";

        mockMvc.perform(post("/api/v1/diaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        Diary savedDiary = diaryRepository.findTopByOrderByIdDesc().orElseThrow();

        assertThat(savedDiary.getDiaryGenres()).isNotEmpty();
    }

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("감상일기 등록 → DiaryOtt 생성")
    void t3() throws Exception {
        tagRepository.save(new Tag("더미"));
        Ott ott1 = ottRepository.save(new Ott("Netflix"));
        Ott ott2 = ottRepository.save(new Ott("Disney+"));

        String body = """
    {
        "title": "인셉션",
        "contentText": "OTT 테스트",
        "rating": 4.0,
        "isPublic": true,
        "externalId": "tt1375666",
        "type": "MOVIE",
        "tagNames": ["더미"],
        "ottIds": [%d, %d]
    }
""".formatted(ott1.getId(), ott2.getId());

        mockMvc.perform(post("/api/v1/diaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        Diary savedDiary = diaryRepository.findTopByOrderByIdDesc().orElseThrow();

        assertThat(savedDiary.getDiaryOtts()).hasSize(2);
        assertThat(savedDiary.getDiaryOtts())
                .extracting(do_ -> do_.getOtt().getName())
                .containsExactlyInAnyOrder("Netflix", "Disney+");
    }

}
