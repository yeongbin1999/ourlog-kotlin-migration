package com.back.ourlog.domain.like.repository

import com.back.ourlog.config.TestConfig
import com.back.ourlog.domain.content.entity.Content
import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.domain.content.repository.ContentRepository
import com.back.ourlog.domain.diary.entity.Diary
import com.back.ourlog.domain.diary.repository.DiaryRepository
import com.back.ourlog.domain.like.entity.Like
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.LocalDate

@DataJpaTest
@Import(TestConfig::class)
class LikeRepositoryTest @Autowired constructor(
    private val likeRepository: LikeRepository,
    private val userRepository: UserRepository,
    private val diaryRepository: DiaryRepository,
    private val contentRepository: ContentRepository,
) {

    private lateinit var user: User
    private lateinit var diary: Diary

    @BeforeEach
    fun setUp() {
        // given: 모든 테스트 전에 실행될 공통 데이터 준비
        user = userRepository.save(User(nickname = "testuser", email = "test@test.com", password = "password"))

        val content = contentRepository.save(
            Content(
                title = "Test Content Title",
                type = ContentType.MOVIE,
                creatorName = "Test Director",
                description = "A movie for testing.",
                posterUrl = "http://example.com/poster.jpg",
                releasedAt = LocalDate.parse("2025-01-01").atStartOfDay(),
                externalId = "ext_12345"
            )
        )

        diary = diaryRepository.save(
            Diary(
                user = user,
                content = content,
                title = "Test Diary Title",
                contentText = "This is diary content text.",
                rating = 4.5f,
                isPublic = true
            )
        )
    }

    @Test
    fun `특정 사용자가 특정 다이어리에 좋아요를 눌렀는지 확인할 수 있다`() {
        // given
        likeRepository.save(Like(user = user, diary = diary))

        // when
        val exists = likeRepository.existsByUserIdAndDiaryId(user.id!!, diary.id!!)
        val notExists = likeRepository.existsByUserIdAndDiaryId(user.id!!, 999)

        // then
        assertThat(exists).isTrue()
        assertThat(notExists).isFalse()
    }

    @Test
    fun `특정 다이어리의 좋아요 개수를 계산할 수 있다`() {
        // given
        val anotherUser = userRepository.save(User(nickname = "user2", email = "user2@test.com", password = "password"))
        likeRepository.save(Like(user = user, diary = diary))
        likeRepository.save(Like(user = anotherUser, diary = diary))

        // when
        val likeCount = likeRepository.countByDiaryId(diary.id!!)

        // then
        assertThat(likeCount).isEqualTo(2)
    }


    @Test
    fun `특정 사용자가 누른 좋아요를 삭제할 수 있다`() {
        // given
        likeRepository.save(Like(user = user, diary = diary))
        assertThat(likeRepository.existsByUserIdAndDiaryId(user.id!!, diary.id!!)).isTrue()

        // when
        likeRepository.deleteByUserIdAndDiaryId(user.id!!, diary.id!!)

        // then
        assertThat(likeRepository.existsByUserIdAndDiaryId(user.id!!, diary.id!!)).isFalse()
    }
}