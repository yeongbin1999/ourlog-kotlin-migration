package com.back.ourlog.domain.like.service

import com.back.ourlog.domain.content.entity.Content
import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.domain.diary.entity.Diary
import com.back.ourlog.domain.diary.repository.DiaryRepository
import com.back.ourlog.domain.like.entity.Like
import com.back.ourlog.domain.like.repository.LikeRepository
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import com.back.ourlog.global.rq.Rq
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime
import java.util.Optional

// 1. @SpringBootTest 대신 @ExtendWith(MockitoExtension::class) 사용
@ExtendWith(MockitoExtension::class)
class LikeServiceTest {

    // 2. @InjectMocks로 테스트 대상 주입, @Mock으로 의존성 가짜 객체로 선언
    @InjectMocks
    private lateinit var likeService: LikeService

    @Mock
    private lateinit var likeRepository: LikeRepository

    @Mock
    private lateinit var diaryRepository: DiaryRepository

    @Mock
    private lateinit var rq: Rq

    @Test
    @DisplayName("좋아요를 처음 누르면 true를 반환하고 데이터가 저장된다")
    fun `like 성공 테스트`() {
        // given - 필요한 데이터와 Mock 객체의 행동 정의
        val diaryId = 1
        val mockUser = User("testuser", "test@test.com", "password").apply { id = 100 }
        val mockContent = Content(
            title = "Test Content",
            type = ContentType.MOVIE,
            creatorName = "Test Director",
            description = "Description",
            posterUrl = "url",
            releasedAt = LocalDateTime.now(),
            externalId = "ext123"
        )
        val mockDiary = Diary(mockUser, mockContent, "title", "content", 5.0f, true)

        whenever(rq.currentUser).thenReturn(mockUser)
        whenever(likeRepository.existsByUserIdAndDiaryId(mockUser.id!!, diaryId)).thenReturn(false)
        whenever(diaryRepository.findById(diaryId)).thenReturn(Optional.of(mockDiary)) // 👈 수정

        // when - 실제 메서드 호출
        val result = likeService.like(diaryId)

        // then - 결과 및 Mock 객체와의 상호작용 검증
        assertThat(result).isTrue()
        verify(likeRepository).save(any<Like>()) // Like 객체로 save가 호출되었는지 검증
    }

    @Test
    @DisplayName("이미 좋아요를 눌렀으면 false를 반환하고 아무 작업도 하지 않는다")
    fun `like 중복 요청 테스트`() {
        // given
        val diaryId = 1
        val mockUser = User("testuser", "test@test.com", "password").apply { id = 100 }

        whenever(rq.currentUser).thenReturn(mockUser)
        whenever(likeRepository.existsByUserIdAndDiaryId(mockUser.id, diaryId)).thenReturn(true)

        // when
        val result = likeService.like(diaryId)

        // then
        assertThat(result).isFalse()
        verify(diaryRepository, never()).findById(any())
        verify(likeRepository, never()).save(any())
    }

    @Test
    @DisplayName("존재하지 않는 다이어리에 좋아요를 누르면 예외가 발생한다")
    fun `like 실패 테스트 - 다이어리 없음`() {
        // given
        val diaryId = 999 // 존재하지 않는 ID
        val mockUser = User("testuser", "test@test.com", "password").apply { id = 100 }

        whenever(rq.currentUser).thenReturn(mockUser)
        whenever(likeRepository.existsByUserIdAndDiaryId(mockUser.id!!, diaryId)).thenReturn(false)
        whenever(diaryRepository.findById(diaryId)).thenReturn(Optional.empty()) // 👈 추가
        // when & then
        val exception = assertThrows<CustomException> {
            likeService.like(diaryId)
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.DIARY_NOT_FOUND)
    }

    @Test
    @DisplayName("unlike 호출 시 delete 메서드가 정상적으로 호출된다")
    fun `unlike 성공 테스트`() {
        // given
        val diaryId = 1
        val mockUser = User("testuser", "test@test.com", "password").apply { id = 100 }
        whenever(rq.currentUser).thenReturn(mockUser)

        // when
        likeService.unlike(diaryId)

        // then
        verify(likeRepository).deleteByUserIdAndDiaryId(mockUser.id!!, diaryId)
    }
}