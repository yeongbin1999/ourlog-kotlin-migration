package com.back.ourlog.domain.timeline.service

import com.back.ourlog.domain.comment.repository.CommentRepository
import com.back.ourlog.domain.content.entity.Content
import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.domain.diary.entity.Diary
import com.back.ourlog.domain.like.repository.LikeRepository
import com.back.ourlog.domain.timeline.repository.TimelineRepository
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.global.rq.Rq
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class TimelineServiceTest {

    @Mock
    private lateinit var timelineRepository: TimelineRepository
    @Mock
    private lateinit var likeRepository: LikeRepository
    @Mock
    private lateinit var commentRepository: CommentRepository
    @Mock
    private lateinit var rq: Rq

    @InjectMocks
    private lateinit var timelineService: TimelineService

    private lateinit var user1: User
    private lateinit var content: Content
    private lateinit var diary1: Diary

    @BeforeEach
    fun setUp() {
        // 'val realUser =' 를 'user1 =' 로 수정하여 클래스 필드에 직접 할당합니다.
        user1 = User("user1@test.com", "pw", "유저1").apply { id = 1 }
        val realContent = Content(
            title = "테스트 콘텐츠", type = ContentType.MOVIE, creatorName = "감독",
            description = "설명", posterUrl = "", releasedAt = LocalDateTime.now(), externalId = "ext-1"
        )
        diary1 = Diary(user1, realContent, "공개 일기", "내용", 4.5f, true).apply {
            id = 100
            createdAt = LocalDateTime.now()
        }
    }

    @Test
    @DisplayName("공개된 일기만 반환되어야 한다")
    fun `공개된 일기만 반환되어야 한다`() {
        // given
        // 2. 이 테스트를 실행하는 데 필요한 Mock 행동만 이곳에 정의합니다.
        given(timelineRepository.findPublicDiaries()).willReturn(listOf(diary1))

        // TimelineService가 내부적으로 호출하는 다른 Mock들의 기본 동작을 설정해줍니다.
        // (이 테스트의 검증 대상은 아니지만, Null 에러 방지를 위해 필요)
        given(likeRepository.countByDiaryId(diary1.id)).willReturn(0)
        given(commentRepository.countByDiaryId(diary1.id)).willReturn(0)

        // when
        val result = timelineService.getPublicTimeline()

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("공개 일기")
    }

    @Test
    @DisplayName("좋아요 수가 정확하게 계산되어야 한다")
    fun `좋아요 수가 정확하게 계산되어야 한다`() {
        // given
        // 1. timelineRepository가 diary1을 반환하도록 설정 (이 부분이 추가되었습니다)
        given(timelineRepository.findPublicDiaries()).willReturn(listOf(diary1))

        // 2. 이 테스트의 핵심 검증 대상인 likeRepository의 행동을 정의
        given(likeRepository.countByDiaryId(diary1.id)).willReturn(5)

        // when
        val result = timelineService.getPublicTimeline()

        // then
        assertThat(result[0].likeCount).isEqualTo(5)
    }


    @Test
    @DisplayName("댓글 수가 정확하게 계산되어야 한다")
    fun `댓글 수가 정확하게 계산되어야 한다`() {
        // given
        // 1. timelineRepository가 diary1을 반환하도록 설정 (이 부분이 추가되었습니다)
        given(timelineRepository.findPublicDiaries()).willReturn(listOf(diary1))

        // 2. 이 테스트의 핵심 검증 대상인 commentRepository의 행동을 정의
        given(commentRepository.countByDiaryId(diary1.id)).willReturn(3)

        // when
        val result = timelineService.getPublicTimeline()

        // then
        assertThat(result[0].commentCount).isEqualTo(3)
    }

    @Test
    @DisplayName("로그인한 유저가 좋아요를 누른 경우 isLiked는 true여야 한다")
    fun `로그인 유저가 좋아요 누른 경우 isLiked는 true`() {
        // given
        given(timelineRepository.findPublicDiaries()).willReturn(listOf(diary1))
        given(rq.currentUser).willReturn(user1)
        given(likeRepository.existsByUserIdAndDiaryId(user1.id, diary1.id)).willReturn(true)

        // when
        val result = timelineService.getPublicTimeline()

        // then
        assertThat(result[0].isLiked).isTrue()
    }

    @Test
    @DisplayName("작성자 정보가 정확하게 반환되어야 한다")
    fun `작성자 정보가 정확하게 반환되어야 한다`() {
        // given
        // 1. timelineRepository가 diary1을 반환하도록 설정 (이 부분이 추가되었습니다)
        given(timelineRepository.findPublicDiaries()).willReturn(listOf(diary1))

        // when
        val result = timelineService.getPublicTimeline()

        // then
        result[0].user?.let { userSummary ->
            assertThat(userSummary.id).isEqualTo(user1.id)
            assertThat(userSummary.nickname).isEqualTo(user1.nickname)
        } ?: fail("UserSummary should not be null")
    }
}