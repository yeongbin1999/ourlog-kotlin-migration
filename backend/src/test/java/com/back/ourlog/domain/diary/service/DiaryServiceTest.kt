package com.back.ourlog.domain.diary.service

import com.back.ourlog.config.ContentTestMockConfig
import com.back.ourlog.domain.content.dto.ContentSearchResultDto
import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.domain.diary.dto.DiaryWriteRequestDto
import com.back.ourlog.domain.diary.repository.DiaryRepository
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.domain.user.entity.User.Companion.createNormalUser
import com.back.ourlog.domain.user.repository.UserRepository
import com.back.ourlog.external.common.ContentSearchFacade
import com.back.ourlog.global.rq.Rq
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@ActiveProfiles("test")
@SpringBootTest
@Import(ContentTestMockConfig::class)
@Transactional
class DiaryServiceTest {

    @Autowired
    lateinit var diaryService: DiaryService

    @Autowired
    lateinit var diaryRepository: DiaryRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var contentSearchFacade: ContentSearchFacade

    @Autowired
    lateinit var rq: Rq

    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        user = userRepository.findByEmail("user1@test.com")
            .orElseGet {
                userRepository.save(
                    createNormalUser("user1@test.com", passwordEncoder.encode("1234"), "테스트유저", null, null)
                )
            }
        `when`(rq.currentUser).thenReturn(user)

        val inception = ContentSearchResultDto(
            externalId = "tt1375666",
            title = "Inception",
            type = ContentType.MOVIE,
            genres = listOf("Sci-Fi", "Thriller"),
            releasedAt = LocalDateTime.now().minusYears(14)
        )
        `when`(contentSearchFacade.search(ContentType.MOVIE, "tt1375666"))
            .thenReturn(inception)
    }

    @Test
    @DisplayName("감상일기 등록 시 DTO 반환 및 연관관계 동기화 확인")
    fun t1() {
        val req = DiaryWriteRequestDto(
            title = "인셉션",
            contentText = "테스트 내용",
            isPublic = true,
            rating = 4.0f,
            type = ContentType.MOVIE,
            externalId = "tt1375666",
            tagNames = listOf("감동", "분노"),
            genreIds = emptyList(),
            ottIds = emptyList()
        )

        val dto = diaryService.writeWithContentSearch(req, user)
        assertThat(dto.title).isEqualTo("인셉션")
        assertThat(dto.tags).containsExactlyInAnyOrder("감동", "분노")

        val saved = diaryRepository.findAll().maxByOrNull { it.id } ?: error("no diary saved")
        assertThat(saved.diaryTags.map { it.tag.name })
            .containsExactlyInAnyOrder("감동", "분노")
        assertThat(saved.diaryGenres).isNotEmpty()
    }
}
