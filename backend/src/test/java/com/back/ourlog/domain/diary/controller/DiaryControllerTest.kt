package com.back.ourlog.domain.diary.controller

import com.back.ourlog.config.ContentTestMockConfig
import com.back.ourlog.domain.content.dto.ContentSearchResultDto
import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.domain.user.entity.User.Companion.createNormalUser
import com.back.ourlog.domain.user.repository.UserRepository
import com.back.ourlog.external.common.ContentSearchFacade
import com.back.ourlog.global.rq.Rq
import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import(ContentTestMockConfig::class)
@Transactional
class DiaryControllerTest {

    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var contentSearchFacade: ContentSearchFacade

    @Autowired lateinit var rq: Rq

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        testUser = userRepository.findByEmail("user1@test.com")
            .orElseGet {
                userRepository.save(
                    createNormalUser(
                        "user1@test.com",
                        passwordEncoder.encode("1234"),
                        "테스트유저",
                        null,
                        null
                    )
                )
            }

        `when`(rq.currentUser).thenReturn(testUser)

        // 외부 검색 스텁
        val inception = ContentSearchResultDto(
            externalId = "tt1375666",
            title = "Inception",
            type = ContentType.MOVIE,
            genres = listOf("Sci-Fi", "Thriller"),
            releasedAt = LocalDateTime.now().minusYears(14)
        )
        `when`(contentSearchFacade.search(ContentType.MOVIE, "tt1375666"))
            .thenReturn(inception)

        val aBook = ContentSearchResultDto(
            externalId = "library-9791190908207",
            title = "어떤 책",
            type = ContentType.BOOK,
            genres = listOf("소설")
        )
        `when`(contentSearchFacade.search(ContentType.BOOK, "library-9791190908207"))
            .thenReturn(aBook)
    }

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("감상일기 등록 성공")
    fun t1() {
        val body = """
            {
              "title": "인셉션",
              "contentText": "정말 재밌었어요!",
              "rating": 4.8,
              "isPublic": true,
              "externalId": "tt1375666",
              "type": "MOVIE",
              "tagNames": ["감동"]
            }
        """.trimIndent()

        mvc.perform(
            post("/api/v1/diaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.msg").value("감상일기가 등록되었습니다."))
            .andExpect(jsonPath("$.data.title").value("인셉션"))
    }

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("감상일기 등록 실패 - 제목 없음")
    fun t2() {
        val body = """
            {
              "title": "",
              "contentText": "내용 있음",
              "rating": 4.0,
              "isPublic": true,
              "externalId": "tt1375666",
              "type": "MOVIE",
              "tagNames": ["슬픔"]
            }
        """.trimIndent()

        mvc.perform(
            post("/api/v1/diaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.resultCode").value("COMMON_400"))
            .andExpect(jsonPath("$.msg").value("제목을 입력해주세요."))
    }

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("감상일기 등록 실패 - 내용 없음")
    fun t3() {
        val body = """
            {
              "title": "제목 있음",
              "contentText": "",
              "rating": 3.0,
              "isPublic": true,
              "type": "BOOK",
              "externalId": "library-9791190908207",
              "tagNames": ["분노"]
            }
        """.trimIndent()

        mvc.perform(
            post("/api/v1/diaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.resultCode").value("COMMON_400"))
            .andExpect(jsonPath("$.msg").value("내용을 입력해주세요."))
    }

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("감상일기 수정 성공")
    fun t4() {
        val createBody = """
            {
              "title": "인셉션",
              "contentText": "원본 내용",
              "rating": 3.5,
              "isPublic": true,
              "externalId": "tt1375666",
              "type": "MOVIE",
              "tagNames": ["감동"]
            }
        """.trimIndent()

        val created = mvc.perform(
            post("/api/v1/diaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody)
        )
            .andExpect(status().isOk)
            .andReturn()

        val id: Int = JsonPath.read(created.response.contentAsString, "\$.data.id")

        val updateBody = """
            {
              "title": "메멘토",
              "contentText": "수정된 내용입니다.",
              "rating": 4.0,
              "isPublic": true,
              "externalId": "tt1375666",
              "type": "MOVIE",
              "tagNames": ["감동", "분노"]
            }
        """.trimIndent()

        mvc.perform(
            put("/api/v1/diaries/$id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.data.title").value("메멘토"))
            .andExpect(jsonPath("$.data.contentText").value("수정된 내용입니다."))
            .andExpect(jsonPath("$.data.rating").value(4.0))
    }

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("감상일기 수정 실패 - 존재하지 않는 OTT ID")
    fun t5() {
        val createBody = """
            {
              "title": "테스트",
              "contentText": "초기 내용",
              "rating": 4.0,
              "isPublic": true,
              "externalId": "tt1375666",
              "type": "MOVIE",
              "tagNames": ["놀람"]
            }
        """.trimIndent()

        val created = mvc.perform(
            post("/api/v1/diaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody)
        )
            .andExpect(status().isOk)
            .andReturn()

        val id: Int = JsonPath.read(created.response.contentAsString, "\$.data.id")

        val updateBody = """
            {
              "title": "업데이트됨",
              "contentText": "내용 수정",
              "rating": 4.2,
              "isPublic": true,
              "externalId": "tt1375666",
              "type": "MOVIE",
              "tagNames": ["놀람"],
              "ottIds": [999]
            }
        """.trimIndent()

        mvc.perform(
            put("/api/v1/diaries/$id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.resultCode").value("OTT_001"))
            .andExpect(jsonPath("$.msg").value("존재하지 않는 OTT입니다."))
    }

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("감상일기 삭제 성공")
    fun t6() {
        val createBody = """
            {
              "title": "삭제 테스트",
              "contentText": "삭제 테스트 내용",
              "rating": 4.0,
              "isPublic": true,
              "externalId": "tt1375666",
              "type": "MOVIE",
              "tagNames": ["감동"]
            }
        """.trimIndent()

        val created = mvc.perform(
            post("/api/v1/diaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody)
        )
            .andExpect(status().isOk)
            .andReturn()

        val id: Int = JsonPath.read(created.response.contentAsString, "\$.data.id")

        mvc.perform(delete("/api/v1/diaries/$id"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.msg").value("일기 삭제 완료"))
    }

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("감상일기 조회 성공")
    fun t7() {
        val createBody = """
            {
              "title": "다이어리 조회 테스트",
              "contentText": "이것은 다이어리 조회 테스트 내용입니다.",
              "rating": 3.0,
              "isPublic": true,
              "externalId": "tt1375666",
              "type": "MOVIE",
              "tagNames": ["감동"]
            }
        """.trimIndent()

        val created = mvc.perform(
            post("/api/v1/diaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody)
        )
            .andExpect(status().isOk)
            .andReturn()

        val id: Int = JsonPath.read(created.response.contentAsString, "\$.data.id")

        mvc.perform(get("/api/v1/diaries/$id"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(handler().handlerType(DiaryController::class.java))
            .andExpect(handler().methodName("getDiary"))
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.data.title").value("다이어리 조회 테스트"))
            .andExpect(jsonPath("$.data.rating").value(3.0))
            .andExpect(jsonPath("$.data.contentText").value("이것은 다이어리 조회 테스트 내용입니다."))
            .andExpect(jsonPath("$.data.tagNames[0]").exists())
    }

    @Test
    @DisplayName("감상일기 조회 실패 - 없는 ID")
    fun t8() {
        mvc.perform(get("/api/v1/diaries/100000"))
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.resultCode").value("DIARY_001"))
            .andExpect(jsonPath("$.msg").value("존재하지 않는 다이어리입니다."))
    }
}
