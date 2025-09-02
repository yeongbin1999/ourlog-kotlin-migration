package com.back.ourlog.domain.timeline.repository

import com.back.ourlog.config.TestConfig
import com.back.ourlog.domain.content.entity.Content
import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.domain.content.repository.ContentRepository
import com.back.ourlog.domain.diary.entity.Diary
import com.back.ourlog.domain.diary.repository.DiaryRepository
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.LocalDateTime

@DataJpaTest // JPA 관련 설정만 로드하여 빠르고 가볍게 테스트합니다.
@Import(TestConfig::class)
class TimelineRepositoryTest {

    // 테스트에 필요한 Repository들을 주입받습니다.
    @Autowired
    private lateinit var timelineRepository: TimelineRepository

    @Autowired
    private lateinit var diaryRepository: DiaryRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var contentRepository: ContentRepository

    @Test
    fun `findPublicDiaries는 공개된 일기만 생성일자 내림차순으로 조회한다`() {
        // Arrange (Given)
        val user = userRepository.save(User(nickname = "testUser", password = "1234", email = "test@test.com"))

        // 2. Diary를 만들기 전, 의존성인 Content 객체를 먼저 생성하고 저장합니다.
        val content = contentRepository.save(
            Content(
                title = "Test Content",
                type = ContentType.MOVIE,
                creatorName = "Test Creator",
                description = "Test Description",
                posterUrl = "",
                releasedAt = LocalDateTime.now(), // (수정!) String -> LocalDateTime 타입으로 변경
                externalId = "test-id-123"
            )
        )

        // 3. Diary 엔티티의 실제 생성자에 맞게 파라미터를 수정합니다.
        // - createdAt 제거
        // - content, contentText, rating 추가
        val oldestPublicDiary = diaryRepository.save(
            Diary(
                user = user,
                content = content, // content 객체 전달
                title = "오래된 공개 일기",
                contentText = "오래된 일기 내용",
                rating = 5.0f, // Float 타입이므로 f 접미사 추가
                isPublic = true
            )
        )
        val privateDiary = diaryRepository.save(
            Diary(
                user = user,
                content = content,
                title = "비공개 일기",
                contentText = "비공개 일기 내용",
                rating = 4.0f,
                isPublic = false
            )
        )
        val newestPublicDiary = diaryRepository.save(
            Diary(
                user = user,
                content = content,
                title = "가장 최신 공개 일기",
                contentText = "최신 일기 내용",
                rating = 3.0f,
                isPublic = true
            )
        )

        // Act (When)
        val result = timelineRepository.findPublicDiaries()

        // Assert (Then)
        assertThat(result).hasSize(2)
        assertThat(result).extracting("isPublic").containsOnly(true)
        assertThat(result[0].id).isEqualTo(newestPublicDiary.id)
        assertThat(result[1].id).isEqualTo(oldestPublicDiary.id)
    }
}