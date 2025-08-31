package com.back.ourlog.domain.follow.controller

import com.back.ourlog.domain.follow.dto.FollowUserResponse
import com.back.ourlog.domain.follow.service.FollowService
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.global.config.JpaAuditingConfig
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import com.back.ourlog.global.rq.Rq
import com.back.ourlog.global.security.config.SecurityConfig
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.mockito.kotlin.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@WebMvcTest(
    controllers = [FollowController::class],
    // ✅ 우리의 커스텀 설정인 SecurityConfig와 JpaAuditingConfig를 테스트에서 명확히 제외합니다.
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [SecurityConfig::class, JpaAuditingConfig::class]
        )
    ]
)
internal class FollowControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    // ✅ 최신 방식인 @MockitoBean을 사용합니다.
    @MockitoBean
    private lateinit var followService: FollowService

    @MockitoBean
    private lateinit var rq: Rq

    @Test
    @WithMockUser // 👈 spring-security-test 의존성 덕분에 이 기능이 정상 동작합니다.
    @DisplayName("성공: 팔로우 요청 시 200 OK와 성공 응답을 반환한다")
    fun `팔로우 요청 성공`() {
        // given
        val followerId = 1
        val followeeId = 2

        // rq.currentUser가 호출될 때 ID를 가진 가짜 유저를 반환하도록 설정합니다.
        val mockFollower = mock<User>()
        whenever(mockFollower.id).thenReturn(followerId)
        whenever(rq.currentUser).thenReturn(mockFollower)

        doNothing().whenever(followService).follow(any(), any())

        // when & then
        mockMvc.perform(
            post("/api/v1/follows/$followeeId")
                .with(csrf())
        )
            .andExpect(status().isOk) // 401 대신 200 OK가 나와야 합니다.
            // ✅ [수정] 예상 값을 "SUCCESS"에서 실제 응답인 "SUCCESS_200"으로 변경
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.msg").value("팔로우 요청했습니다."))
            .andDo(print())

        verify(followService).follow(followerId, followeeId)
    }

    @Test
    @WithMockUser
    @DisplayName("실패: 이미 팔로우한 유저에게 요청 시 409 Conflict와 실패 응답을 반환한다")
    fun `팔로우 요청 실패 - 이미 팔로우한 경우`() {
        // given
        val followerId = 1
        val followeeId = 2
        val mockFollower = mock<User>()
        whenever(mockFollower.id).thenReturn(followerId)
        whenever(rq.currentUser).thenReturn(mockFollower)

        whenever(followService.follow(followerId, followeeId))
            .thenThrow(CustomException(ErrorCode.FOLLOW_ALREADY_EXISTS))

        // when & then
        mockMvc.perform(
            post("/api/v1/follows/$followeeId")
                .with(csrf())
        )
            .andExpect(status().isBadRequest) // 401 대신 409 Conflict가 나와야 합니다.
            .andExpect(jsonPath("$.resultCode").value("FOLLOW_001"))
            .andExpect(jsonPath("$.msg").value("이미 팔로우한 사용자입니다.")) // 메시지도 함께 검증
            .andDo(print())
    }

    @Test
    @WithMockUser // 기본값 "user"라는 이름의 사용자로 로그인
    @DisplayName("성공: 언팔로우 요청 시 200 OK와 성공 응답을 반환한다")
    fun `언팔로우 요청 성공`() {
        // given (준비)
        val myId = 1
        val otherUserId = 2

        // Rq가 ID를 가진 가짜 유저를 반환하도록 설정
        val mockUser = mock<User>()
        whenever(mockUser.id).thenReturn(myId)
        whenever(rq.currentUser).thenReturn(mockUser)

        // followService.unfollow는 성공적으로 실행된다고 가정
        doNothing().whenever(followService).unfollow(any(), any())

        // when & then (실행 및 검증)
        mockMvc.perform(
            delete("/api/v1/follows/$otherUserId") // DELETE 요청
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.msg").value("팔로우 관계를 끊었습니다."))
            .andDo(print())

        // Controller가 Service의 unfollow 메서드를 올바른 인자와 함께 호출했는지 최종 검증
        verify(followService).unfollow(myId, otherUserId)
    }

    @Test
    @WithMockUser
    @DisplayName("실패: 팔로우하지 않는 유저를 언팔로우하면 404 Not Found와 실패 응답을 반환한다")
    fun `언팔로우 요청 실패 - 관계 없음`() {
        // given (준비)
        val myId = 1
        val otherUserId = 2

        val mockUser = mock<User>()
        whenever(mockUser.id).thenReturn(myId)
        whenever(rq.currentUser).thenReturn(mockUser)

        // followService.unfollow가 호출되면 FOLLOW_NOT_FOUND 예외를 던지도록 설정
        whenever(followService.unfollow(myId, otherUserId))
            .thenThrow(CustomException(ErrorCode.FOLLOW_NOT_FOUND))

        // when & then (실행 및 검증)
        mockMvc.perform(
            delete("/api/v1/follows/$otherUserId")
                .with(csrf())
        )
            .andExpect(status().isNotFound) // ErrorCode에 정의된 404 상태를 반환하는지 확인
            .andExpect(jsonPath("$.resultCode").value("FOLLOW_002"))
            .andExpect(jsonPath("$.msg").value("팔로우 관계가 존재하지 않습니다."))
            .andDo(print())
    }
    @Test
    @WithMockUser
    @DisplayName("성공: 내가 팔로우한 목록 조회 시 200 OK와 DTO 리스트를 반환한다")
    fun `팔로잉 목록 조회 성공`() {
        // given (준비)
        val myId = 1
        val mockUser = mock<User>()
        whenever(mockUser.id).thenReturn(myId)
        whenever(rq.currentUser).thenReturn(mockUser)

        // Service가 반환할 가짜 DTO 리스트를 미리 준비합니다.
        val mockResponseList = listOf(
            FollowUserResponse(2, "user2@test.com", "팔로잉1", null, 101, true),
            FollowUserResponse(3, "user3@test.com", "팔로잉2", "url/p3.jpg", 102, true)
        )

        // followService.getFollowings가 호출되면, 위에서 만든 가짜 리스트를 반환하도록 설정
        whenever(followService.getFollowings(myId)).thenReturn(mockResponseList)

        // when & then (실행 및 검증)
        mockMvc.perform(
            get("/api/v1/follows/followings") // GET 요청
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.data.size()").value(2)) // 데이터가 2개인지 확인
            .andExpect(jsonPath("$.data[0].nickname").value("팔로잉1"))
            .andExpect(jsonPath("$.data[1].nickname").value("팔로잉2"))
            .andDo(print())
    }

    @Test
    @WithMockUser
    @DisplayName("성공: 팔로우한 유저가 없을 경우 200 OK와 빈 리스트를 반환한다")
    fun `팔로잉 목록이 없을 때`() {
        // given
        val myId = 1
        val mockUser = mock<User>()
        whenever(mockUser.id).thenReturn(myId)
        whenever(rq.currentUser).thenReturn(mockUser)

        // Service가 빈 리스트를 반환하는 상황을 가정
        whenever(followService.getFollowings(myId)).thenReturn(emptyList())

        // when & then
        mockMvc.perform(
            get("/api/v1/follows/followings")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.data.size()").value(0)) // 데이터가 0개인지 확인
            .andDo(print())
    }

    @Test
    @WithMockUser // 이 테스트는 "user"라는 이름의 사용자로 로그인됩니다.
    @DisplayName("GET /api/v1/follows/followers : 팔로워 목록 조회 성공")
    fun `팔로워 목록 조회 성공`() {
        // given
        val myId = 1 // 테스트의 명확성을 위해 ID를 직접 지정

        // ✅ 테스트 메서드 안에서 직접 로그인한 사용자를 설정합니다.
        val mockUser = mock<User>()
        whenever(mockUser.id).thenReturn(myId)
        whenever(rq.currentUser).thenReturn(mockUser) // rq.currentUser가 mockUser를 반환하도록 설정

        val mockResponseList = listOf(
            FollowUserResponse(2, "user2@test.com", "나를팔로우한사람1", null, 101, true)
        )
        whenever(followService.getFollowers(myId)).thenReturn(mockResponseList)

        // when & then
        mockMvc.perform(get("/api/v1/follows/followers"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.data[0].nickname").value("나를팔로우한사람1"))
            .andDo(print())
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/follows/followers : 팔로워 목록이 없을 때")
    fun `팔로워 목록이 없을 때`() {
        // given
        val myId = 1

        // ✅ 이 테스트에서도 로그인한 사용자를 별도로 설정합니다.
        val mockUser = mock<User>()
        whenever(mockUser.id).thenReturn(myId)
        whenever(rq.currentUser).thenReturn(mockUser)

        whenever(followService.getFollowers(myId)).thenReturn(emptyList())

        // when & then
        mockMvc.perform(get("/api/v1/follows/followers"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.data").isEmpty)
            .andDo(print())
    }

    @Test
    @WithMockUser
    @DisplayName("성공: 팔로우 요청 수락 시 200 OK와 성공 응답을 반환한다")
    fun `팔로우 요청 수락 성공`() {
        // given
        val followId = 101
        // followService.acceptFollow는 성공적으로 실행된다고 가정
        doNothing().whenever(followService).acceptFollow(any())

        // when & then
        mockMvc.perform(
            post("/api/v1/follows/$followId/accept")
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.msg").value("팔로우 요청 수락 완료!"))
            .andDo(print())

        // Controller가 Service의 acceptFollow 메서드를 올바른 followId로 호출했는지 검증
        verify(followService).acceptFollow(followId)
    }

    @Test
    @WithMockUser
    @DisplayName("실패: 이미 수락된 요청을 수락하면 400 Bad Request와 실패 응답을 반환한다")
    fun `팔로우 요청 수락 실패 - 이미 수락된 경우`() {
        // given
        val followId = 101
        // followService.acceptFollow가 호출되면 FOLLOW_ALREADY_ACCEPTED 예외를 던지도록 설정
        whenever(followService.acceptFollow(followId))
            .thenThrow(CustomException(ErrorCode.FOLLOW_ALREADY_ACCEPTED))

        // when & then
        mockMvc.perform(
            post("/api/v1/follows/$followId/accept")
                .with(csrf())
        )
            .andExpect(status().isBadRequest) // 400 Bad Request 예상
            .andExpect(jsonPath("$.resultCode").value("FOLLOW_005")) // ErrorCode에 정의된 코드
            .andExpect(jsonPath("$.msg").value("이미 수락된 팔로우 요청입니다."))
            .andDo(print())
    }

    @Test
    @WithMockUser
    @DisplayName("성공: 팔로우 요청 거절 시 200 OK와 성공 응답을 반환한다")
    fun `팔로우 요청 거절 성공`() {
        // given
        val followId = 101
        doNothing().whenever(followService).rejectFollow(any())

        // when & then
        mockMvc.perform(
            delete("/api/v1/follows/$followId/reject")
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.msg").value("팔로우 요청 거절 완료!"))
            .andDo(print())

        verify(followService).rejectFollow(followId)
    }

    @Test
    @WithMockUser
    @DisplayName("실패: 이미 거절된 요청을 거절하면 400 Bad Request와 실패 응답을 반환한다")
    fun `팔로우 요청 거절 실패 - 이미 거절된 경우`() {
        // given
        val followId = 101
        whenever(followService.rejectFollow(followId))
            .thenThrow(CustomException(ErrorCode.FOLLOW_ALREADY_REJECTED))

        // when & then
        mockMvc.perform(
            delete("/api/v1/follows/$followId/reject")
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.resultCode").value("FOLLOW_004"))
            .andExpect(jsonPath("$.msg").value("이미 거절한 팔로우 요청입니다."))
            .andDo(print())
    }

    @Test
    @WithMockUser
    @DisplayName("실패: 이미 수락된 요청을 거절하면 400 Bad Request와 실패 응답을 반환한다")
    fun `팔로우 요청 거절 실패 - 이미 수락된 경우`() {
        // given
        val followId = 101
        whenever(followService.rejectFollow(followId))
            .thenThrow(CustomException(ErrorCode.FOLLOW_ALREADY_ACCEPTED))

        // when & then
        mockMvc.perform(
            delete("/api/v1/follows/$followId/reject")
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.resultCode").value("FOLLOW_005"))
            .andExpect(jsonPath("$.msg").value("이미 수락된 팔로우 요청입니다."))
            .andDo(print())
    }

    @Test
    @WithMockUser
    @DisplayName("성공: 내가 보낸 PENDING 요청 목록 조회 시 200 OK와 DTO 리스트를 반환한다")
    fun `보낸 요청 목록 조회 성공`() {
        // given
        val myId = 1
        val mockUser = mock<User>()
        whenever(mockUser.id).thenReturn(myId)
        whenever(rq.currentUser).thenReturn(mockUser)

        // Service가 반환할 가짜 DTO 리스트를 미리 준비
        val mockResponseList = listOf(
            FollowUserResponse(2, "user2@test.com", "요청보낸상대1", null, 101, false),
            FollowUserResponse(3, "user3@test.com", "요청보낸상대2", null, 102, false)
        )

        // followService.getSentPendingRequests가 호출되면, 위에서 만든 가짜 리스트를 반환하도록 설정
        whenever(followService.getSentPendingRequests(myId)).thenReturn(mockResponseList)

        // when & then
        mockMvc.perform(
            get("/api/v1/follows/sent-requests")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.data.size()").value(2))
            .andExpect(jsonPath("$.data[0].nickname").value("요청보낸상대1"))
            .andDo(print())
    }

    @Test
    @WithMockUser
    @DisplayName("성공: 내가 보낸 PENDING 요청이 없을 경우 200 OK와 빈 리스트를 반환한다")
    fun `보낸 요청 목록이 없을 때`() {
        // given
        val myId = 1
        val mockUser = mock<User>()
        whenever(mockUser.id).thenReturn(myId)
        whenever(rq.currentUser).thenReturn(mockUser)

        // Service가 빈 리스트를 반환하는 상황을 가정
        whenever(followService.getSentPendingRequests(myId)).thenReturn(emptyList())

        // when & then
        mockMvc.perform(
            get("/api/v1/follows/sent-requests")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.data").isEmpty)
            .andDo(print())
    }

    @Test
    @WithMockUser
    @DisplayName("성공: 내가 받은 PENDING 요청 목록 조회 시 200 OK와 DTO 리스트를 반환한다")
    fun `받은 요청 목록 조회 성공`() {
        // given
        val myId = 1
        val mockUser = mock<User>()
        whenever(mockUser.id).thenReturn(myId)
        whenever(rq.currentUser).thenReturn(mockUser)

        // Service가 반환할 가짜 DTO 리스트를 미리 준비
        val mockResponseList = listOf(
            FollowUserResponse(2, "user2@test.com", "나에게요청보낸사람1", null, 101, false)
        )

        // followService.getPendingRequests가 호출되면, 위에서 만든 가짜 리스트를 반환하도록 설정
        whenever(followService.getPendingRequests(myId)).thenReturn(mockResponseList)

        // when & then
        mockMvc.perform(
            get("/api/v1/follows/requests")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.data.size()").value(1))
            .andExpect(jsonPath("$.data[0].nickname").value("나에게요청보낸사람1"))
            .andDo(print())
    }

    @Test
    @WithMockUser
    @DisplayName("성공: 내가 받은 PENDING 요청이 없을 경우 200 OK와 빈 리스트를 반환한다")
    fun `받은 요청 목록이 없을 때`() {
        // given
        val myId = 1
        val mockUser = mock<User>()
        whenever(mockUser.id).thenReturn(myId)
        whenever(rq.currentUser).thenReturn(mockUser)

        // Service가 빈 리스트를 반환하는 상황을 가정
        whenever(followService.getPendingRequests(myId)).thenReturn(emptyList())

        // when & then
        mockMvc.perform(
            get("/api/v1/follows/requests")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
            .andExpect(jsonPath("$.data").isEmpty)
            .andDo(print())
    }
}