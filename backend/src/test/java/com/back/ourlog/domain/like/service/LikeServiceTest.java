package com.back.ourlog.domain.like.service;

import com.back.ourlog.domain.content.entity.Content;
import com.back.ourlog.domain.content.entity.ContentType;
import com.back.ourlog.domain.content.repository.ContentRepository;
import com.back.ourlog.domain.diary.entity.Diary;
import com.back.ourlog.domain.diary.repository.DiaryRepository;
import com.back.ourlog.domain.like.repository.LikeRepository;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.domain.user.repository.UserRepository;
import com.back.ourlog.global.rq.Rq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class LikeServiceTest {

    @Autowired private LikeService likeService;
    @Autowired private UserRepository userRepository;
    @Autowired private DiaryRepository diaryRepository;
    @Autowired private LikeRepository likeRepository;
    @Autowired private ContentRepository contentRepository;
    @Autowired private Rq rq;

    private Diary testDiary;


    @BeforeEach
    void setUp() {
        User writer = userRepository.findByEmail("user1@test.com")
                .orElseThrow(() -> new RuntimeException("테스트 유저 없음"));
        // ✅ Content 더미 먼저 저장!
        Content dummyContent = new Content(
                "테스트 콘텐츠 제목",
                ContentType.MOVIE,     // enum 중 하나 사용
                "감독 이름",
                "설명",
                "http://image.url",     // 또는 null
                LocalDateTime.now(),
                "ext123"
        );
        dummyContent = contentRepository.save(dummyContent);

        // ✅ 그다음 Diary에 연결
        testDiary = diaryRepository.save(new Diary(
                writer,
                dummyContent,
                "테스트 다이어리 제목",
                "테스트 내용",
                5.0f,
                true
        ));
    }

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("좋아요 등록 통합 테스트")
    void likeTest() {
        boolean liked = likeService.like(testDiary.getId());

        assertTrue(liked);
        assertEquals(1, likeRepository.countByDiaryId(testDiary.getId()));
    }

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("좋아요 취소 통합 테스트")
    void unlikeTest() {
        // 먼저 좋아요 누르고
        likeService.like(testDiary.getId());

        // 좋아요 취소
        likeService.unlike(testDiary.getId());

        // 좋아요 수는 0이어야 함
        assertEquals(0, likeRepository.countByDiaryId(testDiary.getId()));
    }

    @Test
    @DisplayName("서로 다른 유저가 좋아요를 누르면 총 개수는 정확해야 한다")
    @Transactional
    void multipleUserLikeTest() {
        // given
        User user1 = userRepository.findByEmail("user1@test.com").orElseThrow();
        User user2 = userRepository.findByEmail("user2@test.com").orElseThrow();

        Content dummyContent = contentRepository.save(new Content(
                "다중 테스트 콘텐츠",
                ContentType.MOVIE,
                "감독이름",
                "설명",
                "https://image",
                LocalDateTime.now(),
                "external"
        ));

        Diary diary = diaryRepository.save(new Diary(
                user1,
                dummyContent,
                "제목",
                "내용",
                5.0f,
                true
        ));

        // when - 유저 1이 좋아요
        rq.setMockUser(user1); // ✅ 테스트용 인증 유저 주입
        likeService.like(diary.getId());

        // when - 유저 2가 좋아요
        rq.setMockUser(user2);
        likeService.like(diary.getId());

        // then
        int likeCount = likeRepository.countByDiaryId(diary.getId());
        assertEquals(2, likeCount);
    }

    @Test
    @DisplayName("로그인하지 않은 유저는 좋아요를 누를 수 없다")
    @WithAnonymousUser
        // 시큐리티 비로그인 상태
    void likeWithoutLoginTest() {
        assertThrows(NullPointerException.class, () -> {
            likeService.like(testDiary.getId());
        });
    }

}