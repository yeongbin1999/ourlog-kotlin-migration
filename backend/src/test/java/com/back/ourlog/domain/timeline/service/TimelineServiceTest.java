package com.back.ourlog.domain.timeline.service;

import com.back.ourlog.domain.content.entity.Content;
import com.back.ourlog.domain.content.entity.ContentType;
import com.back.ourlog.domain.content.repository.ContentRepository;
import com.back.ourlog.domain.diary.entity.Diary;
import com.back.ourlog.domain.diary.repository.DiaryRepository;
import com.back.ourlog.domain.like.service.LikeService;
import com.back.ourlog.domain.timeline.dto.TimelineResponse;
import com.back.ourlog.domain.timeline.repository.TimelineRepository;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.domain.user.repository.UserRepository;
import com.back.ourlog.global.rq.Rq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class TimelineServiceTest {

    @Autowired
    private TimelineService timelineService;
    @Autowired private TimelineRepository timelineRepository;
    @Autowired private DiaryRepository diaryRepository;
    @Autowired private ContentRepository contentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private Rq rq;
    @Autowired private LikeService likeService;

    private User user;
    private Content content;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        diaryRepository.deleteAll();
        diaryRepository.flush();
        contentRepository.deleteAll();

        user1 = userRepository.findByEmail("user1@test.com")
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email("user1@test.com")
                                .password("pw")
                                .nickname("유저1")
                                .build()
                ));

        user2 = userRepository.findByEmail("user2@test.com")
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email("user2@test.com")
                                .password("pw")
                                .nickname("유저2")
                                .build()
                ));

        content = contentRepository.save(new Content(
                "테스트 콘텐츠",
                ContentType.MOVIE,
                "감독이름",
                "설명",
                "https://image.com",
                LocalDateTime.now(),
                "external-id"
        ));
    }


    @Test
    @DisplayName("공개된 일기만 반환되는지 테스트")
    void testOnlyPublicDiariesReturned() {
        // given: 공개 일기와 비공개 일기 작성
        diaryRepository.save(new Diary(user1, content, "공개", "내용", 4.5f, true));
        diaryRepository.save(new Diary(user1, content, "비공개", "내용", 3.0f, false));

        // when
        List<TimelineResponse> result = timelineService.getPublicTimeline();

        // then
        assertEquals(1, result.size());
        assertEquals("공개", result.get(0).getTitle());
    }

    @DisplayName("여러 유저가 좋아요를 눌렀을 때 정확한 좋아요 수 반환")
    @Test
    void testLikeCountAccuracy() {
        // given
        Diary diary = diaryRepository.save(new Diary(user1, content, "일기 제목", "일기 내용", 4.0f, true));

        // user1이 로그인했다고 가정
        rq.setMockUser(user1);
        likeService.like(diary.getId());

        // user2이 로그인했다고 가정
        rq.setMockUser(user2);
        likeService.like(diary.getId());

        // when
        int likeCount = likeService.getLikeCount(diary.getId());

        // then
        assertEquals(2, likeCount);
    }

    @Test
    @DisplayName("댓글 수 정확성 테스트")
    void testCommentCountAccuracy() {
        // given
        Diary diary = diaryRepository.save(new Diary(user1, content, "댓글 테스트", "내용", 5.0f, true));

        // user1이 댓글 작성
        diary.addComment(user1, "첫 번째 댓글");

        // user2가 댓글 작성
        diary.addComment(user2, "두 번째 댓글");

        // 댓글을 저장하려면 diaryRepository에 저장된 diary를 다시 flush 해줘야 반영됨
        diaryRepository.save(diary);
        diaryRepository.flush();

        // when
        List<TimelineResponse> result = timelineService.getPublicTimeline();

        // then
        assertEquals(1, result.size());
        TimelineResponse timeline = result.get(0);
        assertEquals(2, timeline.getCommentCount());
    }

    @Test
    @DisplayName("로그인한 유저가 좋아요를 누른 경우 isLiked 값은 true 여야 한다")
    void testIsLikedFieldAccuracy() {
        // given
        Diary diary = diaryRepository.save(new Diary(user1, content, "일기 제목", "내용", 4.5f, true));

        rq.setMockUser(user1); // 로그인한 유저를 user1으로 설정
        likeService.like(diary.getId()); // user1이 좋아요 누름

        // when
        List<TimelineResponse> result = timelineService.getPublicTimeline();

        // then
        TimelineResponse target = result.stream()
                .filter(r -> r.getId().equals(diary.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("해당 일기를 찾을 수 없음"));

        assertEquals(true, target.isLiked(), "user1이 좋아요를 누른 일기에서 isLiked는 true 여야 한다");
    }

    @Test
    @DisplayName("작성자 정보가 정확하게 담겨서 반환되는지 테스트")
    void testUserSummaryAccuracy() {
        // given
        User writer = user1;
        Diary diary = diaryRepository.save(new Diary(writer, content, "타이틀", "내용", 4.0f, true));

        rq.setMockUser(writer);

        // when
        List<TimelineResponse> result = timelineService.getPublicTimeline();
        TimelineResponse response = result.get(0);

        // then
        assertEquals(writer.getId(), response.getUser().getId());
        assertEquals(writer.getNickname(), response.getUser().getNickname());
        assertEquals(writer.getProfileImageUrl(), response.getUser().getProfileImageUrl());
    }

}
