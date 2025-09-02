package com.back.ourlog.domain.content.controller

import com.back.ourlog.config.ContentTestMockConfig
import com.back.ourlog.domain.content.dto.ContentSearchResultDto
import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.external.common.ContentSearchFacade
import com.back.ourlog.global.rq.Rq
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContentTestMockConfig::class)
@Transactional
internal class ContentControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var contentSearchFacade: ContentSearchFacade

    @Autowired
    lateinit var rq: Rq

    @Autowired
    lateinit var testUserFactory: (Int) -> User

    @BeforeEach
    fun setUpAuth() {
        val mockUser = testUserFactory.invoke(18)

        given(rq.currentUser).willReturn(mockUser)
    }


    @Test
    @DisplayName("컨텐츠 조회")
    fun t1() {
        val diaryId = 1

        mvc.perform(get("/api/v1/contents/$diaryId"))
            .andDo(print())
            .andExpect(handler().handlerType(ContentController::class.java))
            .andExpect(handler().methodName("getContentForDiary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("${diaryId}번 다이어리의 컨텐츠 조회 성공"))
            .andExpect(jsonPath("$.data.title").value("콘텐츠 30"))
    }

    @Test
    @DisplayName("컨텐츠 검색 - BOOK")
    fun t2() {
        val mockResult = ContentSearchResultDto(
            externalId = "9788954616515",
            title = "희랍어 시간",
            creatorName = "한강",
            description = null,
            posterUrl = "http://example.com/image.jpg",
            releasedAt = LocalDateTime.of(2011, 11, 10, 0, 0),
            type = ContentType.BOOK,
            genres = listOf("문학", "한국소설")
        )

        given(contentSearchFacade.searchByTitle(ContentType.BOOK, "희랍어 시간"))
            .willReturn(listOf(mockResult))

        mvc.perform(
            MockMvcRequestBuilders.get("/api/v1/contents/search")
                .param("type", ContentType.BOOK.name)
                .param("title", "희랍어 시간")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("콘텐츠 검색 성공"))
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].type").value("BOOK"))
            .andExpect(jsonPath("$.data[0].title").value("희랍어 시간"))
    }

    @Test
    @DisplayName("컨텐츠 검색 - MOVIE")
    fun t3() {
        val mockResult = ContentSearchResultDto(
            externalId = "tt0091530",
            title = "미션",
            creatorName = "롤랑 조페",
            description = "설명 생략",
            posterUrl = "http://example.com/poster.jpg",
            releasedAt = LocalDateTime.of(1986, 9, 18, 0, 0),
            type = ContentType.MOVIE,
            genres = listOf("드라마", "역사")
        )

        given(contentSearchFacade.searchByTitle(ContentType.MOVIE, "미션"))
            .willReturn(listOf(mockResult))

        mvc.perform(
            MockMvcRequestBuilders.get("/api/v1/contents/search")
                .param("type", ContentType.MOVIE.name)
                .param("title", "미션")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("콘텐츠 검색 성공"))
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].type").value("MOVIE"))
            .andExpect(jsonPath("$.data[0].title").value("미션"))
    }

    @Test
    @DisplayName("컨텐츠 검색 - MUSIC")
    fun t4() {
        mvc.perform(
            MockMvcRequestBuilders.get("/api/v1/contents/search")
                .param("type", ContentType.MUSIC.name)
                .param("title", "Always")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(handler().handlerType(ContentController::class.java))
            .andExpect(handler().methodName("searchContents"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("콘텐츠 검색 성공"))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(Matchers.lessThanOrEqualTo(10)))
            .andExpect(jsonPath("$.data[*].type").value(Matchers.everyItem(Matchers.equalTo("MUSIC"))))
    }

    @Test
    @DisplayName("컨텐츠 검색 - 필수 파라미터 누락 시 실패")
    fun t5() {
        mvc.perform(
            MockMvcRequestBuilders.get("/api/v1/contents/search")
                .param("type", "BOOK") // title 누락
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.msg").value("필수 요청 파라미터 'title'가 누락되었습니다."))
    }
}
