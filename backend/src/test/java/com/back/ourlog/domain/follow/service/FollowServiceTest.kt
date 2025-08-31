package com.back.ourlog.domain.follow.service

import com.back.ourlog.domain.follow.entity.Follow
import com.back.ourlog.domain.follow.enums.FollowStatus
import com.back.ourlog.domain.follow.repository.FollowRepository
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.domain.user.repository.UserRepository
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

import org.springframework.data.repository.findByIdOrNull
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class FollowServiceTest {

    @Mock
    private lateinit var followRepository: FollowRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var followService: FollowService

    private val followerId = 1
    private val followeeId = 2

    @Test
    @DisplayName("성공(Case 4): 새로운 팔로우 요청 시 PENDING 상태로 저장된다")
    fun `새로운 팔로우 요청`() {
        // given
        val follower = User.createNormalUser("follower@test.com", "pw", "팔로워")
        val followee = User.createNormalUser("followee@test.com", "pw", "팔로이")
        whenever(userRepository.findById(followerId)).thenReturn(Optional.of(follower))
        whenever(userRepository.findById(followeeId)).thenReturn(Optional.of(followee))
        whenever(followRepository.findByFollowerIdAndFolloweeId(any(), any())).thenReturn(null)

        // when
        followService.follow(followerId, followeeId)

        // then
        val captor = ArgumentCaptor.forClass(Follow::class.java)
        verify(followRepository).save(captor.capture())
        assertThat(captor.value.status).isEqualTo(FollowStatus.PENDING)
        assertThat(captor.value.follower).isEqualTo(follower)
    }

    @Test
    @DisplayName("성공(Case 4): REJECTED 상태의 관계가 있으면 삭제 후 PENDING으로 새로 저장된다")
    fun `거절된 요청에 다시 팔로우`() {
        // given
        val follower = User.createNormalUser("follower@test.com", "pw", "팔로워")
        val followee = User.createNormalUser("followee@test.com", "pw", "팔로이")
        val rejectedFollow = Follow(follower, followee).apply { reject() }
        whenever(userRepository.findById(followerId)).thenReturn(Optional.of(follower))
        whenever(userRepository.findById(followeeId)).thenReturn(Optional.of(followee))
        whenever(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(rejectedFollow)
        whenever(followRepository.findByFollowerIdAndFolloweeId(followeeId, followerId)).thenReturn(null)

        // when
        followService.follow(followerId, followeeId)

        // then
        verify(followRepository).delete(rejectedFollow)
        verify(followRepository).save(any<Follow>())
    }

    @Test
    @DisplayName("성공(Case 4): 상대방이 나를 팔로우 중(ACCEPTED)일 때도 PENDING 요청을 보낸다")
    fun `나를 팔로우하는 사람에게 맞팔 요청`() {
        // given
        val follower = User.createNormalUser("follower@test.com", "pw", "팔로워")
        val followee = User.createNormalUser("followee@test.com", "pw", "팔로이")
        val reversedFollow = Follow(followee, follower).apply { accept() }
        whenever(userRepository.findById(followerId)).thenReturn(Optional.of(follower))
        whenever(userRepository.findById(followeeId)).thenReturn(Optional.of(followee))
        whenever(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(null)
        whenever(followRepository.findByFollowerIdAndFolloweeId(followeeId, followerId)).thenReturn(reversedFollow)

        // when
        followService.follow(followerId, followeeId)

        // then
        val captor = ArgumentCaptor.forClass(Follow::class.java)
        verify(followRepository).save(captor.capture())
        assertThat(captor.value.status).isEqualTo(FollowStatus.PENDING)
    }

    @Test
    @DisplayName("실패(Case 1): 이미 PENDING 상태면 FOLLOW_ALREADY_EXISTS 예외가 발생한다")
    fun `이미 PENDING일 때 요청`() {
        // given
        val existingFollow = Follow(mock(), mock())
        whenever(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(existingFollow)

        // when & then
        val e = assertThrows<CustomException> { followService.follow(followerId, followeeId) }
        assertThat(e.errorCode).isEqualTo(ErrorCode.FOLLOW_ALREADY_EXISTS)
    }

    @Test
    @DisplayName("실패(Case 1): 이미 수락된(ACCEPTED) 상태면 FOLLOW_ALREADY_EXISTS 예외가 발생한다")
    fun `이미 수락된 관계일 때 요청`() {
        // given
        val existingFollow = Follow(mock(), mock()).apply { accept() }
        whenever(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(existingFollow)

        // when & then
        val e = assertThrows<CustomException> { followService.follow(followerId, followeeId) }
        assertThat(e.errorCode).isEqualTo(ErrorCode.FOLLOW_ALREADY_EXISTS)
    }

    @Test
    @DisplayName("실패(Case 2): 상대방의 요청이 PENDING이면 FOLLOW_REQUEST_EXISTS 예외가 발생한다")
    fun `상대방의 요청이 PENDING일 때 요청`() {
        // given
        val reversedFollow = Follow(mock(), mock())
        whenever(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(null)
        whenever(followRepository.findByFollowerIdAndFolloweeId(followeeId, followerId)).thenReturn(reversedFollow)

        // when & then
        val e = assertThrows<CustomException> { followService.follow(followerId, followeeId) }
        assertThat(e.errorCode).isEqualTo(ErrorCode.FOLLOW_REQUEST_EXISTS)
    }

    @Test
    @DisplayName("실패: 자기 자신을 팔로우하면 CANNOT_FOLLOW_SELF 예외가 발생한다")
    fun `자기 자신을 팔로우`() {
        // given
        val userId = 1

        // when & then
        val e = assertThrows<CustomException> { followService.follow(userId, userId) }
        assertThat(e.errorCode).isEqualTo(ErrorCode.CANNOT_FOLLOW_SELF)
        verify(followRepository, never()).save(any())
    }

    @Test
    @DisplayName("성공: 팔로우 관계가 존재할 때 언팔로우가 성공한다")
    fun `언팔로우 성공`() {
        // given: followerId가 followeeId를 팔로우하고 있는 상황을 가정
        // User 객체에 Mockito의 spy를 사용해 실제 메서드(decrease...Count) 호출을 추적할 수 있게 합니다.
        val follower = spy(User.createNormalUser("follower@test.com", "pw", "팔로워"))
        val followee = spy(User.createNormalUser("followee@test.com", "pw", "팔로이"))
        val existingFollow = Follow(follower, followee)

        // unfollow 메서드가 findBy...를 호출하면, 우리가 만든 existingFollow 객체를 반환하도록 설정
        whenever(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(existingFollow)

        // when: 언팔로우 실행
        followService.unfollow(followerId, otherUserId = followeeId)

        // then:
        // 1. followRepository.delete()가 정확한 객체를 가지고 호출되었는지 검증
        verify(followRepository).delete(existingFollow)
        // 2. 각 유저의 카운트 감소 메서드가 정확히 1번씩 호출되었는지 검증
        verify(follower).decreaseFollowingsCount()
        verify(followee).decreaseFollowersCount()
    }

    @Test
    @DisplayName("실패: 팔로우 관계가 존재하지 않으면 예외가 발생한다")
    fun `언팔로우 실패 - 관계 없음`() {
        // given: 팔로우 관계가 존재하지 않는 상황을 가정
        // unfollow 메서드가 findBy...를 호출하면, null을 반환하도록 설정
        whenever(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(null)

        // when & then: 언팔로우 실행 시 예외가 발생하는지 검증
        val e = assertThrows<CustomException> {
            followService.unfollow(followerId, otherUserId = followeeId)
        }

        // 발생한 예외가 우리가 예상한 FOLLOW_NOT_FOUND 예외인지 확인
        assertThat(e.errorCode).isEqualTo(ErrorCode.FOLLOW_NOT_FOUND)

        // 추가 검증: 예외가 발생했으니, delete 메서드는 절대 호출되지 않았음을 보장
        verify(followRepository, never()).delete(any())
    }

    @Test
    @DisplayName("성공: 내가 팔로우한 유저 목록을 조회한다")
    fun `내가 팔로우한 유저 목록 조회`() {
        // given
        val me = User.createNormalUser("me@test.com", "pw", "나").apply { id = 1 }
        val followee1 = User.createNormalUser("f1@test.com", "pw", "팔로이1").apply { id = 2 }
        val followee2 = User.createNormalUser("f2@test.com", "pw", "팔로이2").apply { id = 3 }

        val follow1 = Follow(me, followee1).apply { accept() }
        val follow2 = Follow(me, followee2).apply { accept() }

        whenever(userRepository.findById(me.id!!)).thenReturn(Optional.of(me))
        whenever(
            followRepository.findFollowingsByFollowerIdAndStatus(me.id!!, FollowStatus.ACCEPTED)
        ).thenReturn(listOf(follow1, follow2))

        // when
        val result = followService.getFollowings(me.id!!)

        // then
        assertThat(result).hasSize(2)

        assertThat(result[0].userId).isEqualTo(followee1.id)
        assertThat(result[0].nickname).isEqualTo(followee1.nickname)
        assertThat(result[0].isFollowing).isTrue()

        assertThat(result[1].userId).isEqualTo(followee2.id)
        assertThat(result[1].nickname).isEqualTo(followee2.nickname)
        assertThat(result[1].isFollowing).isTrue()
    }

    @Test
    @DisplayName("성공: 나를 팔로우한 유저 목록을 조회한다 (맞팔 포함)")
    fun `나를 팔로우한 유저 목록 조회`() {
        // given
        val me = User.createNormalUser("me@test.com", "pw", "나").apply { id = 1 }
        val follower1 = User.createNormalUser("f1@test.com", "pw", "팔로워1").apply { id = 2 }
        val follower2 = User.createNormalUser("f2@test.com", "pw", "팔로워2").apply { id = 3 }

        // follower1, follower2 -> me (나를 팔로우)
        val follow1 = Follow(follower1, me).apply { accept() }
        val follow2 = Follow(follower2, me).apply { accept() }

        // 내가 follow1만 맞팔로우한 상태
        val myFollowBack = Follow(me, follower1).apply { accept() }

        whenever(userRepository.findById(me.id!!)).thenReturn(Optional.of(me))
        whenever(followRepository.findFollowersByFolloweeIdAndStatus(me.id!!, FollowStatus.ACCEPTED))
            .thenReturn(listOf(follow1, follow2))
        whenever(followRepository.findFollowingsByFollowerIdAndStatus(me.id!!, FollowStatus.ACCEPTED))
            .thenReturn(listOf(myFollowBack))

        // when
        val result = followService.getFollowers(me.id!!)

        // then
        assertThat(result).hasSize(2)

        // follower1 -> 맞팔
        assertThat(result[0].userId).isEqualTo(follower1.id)
        assertThat(result[0].nickname).isEqualTo(follower1.nickname)
        assertThat(result[0].isFollowing).isTrue()

        // follower2 -> 맞팔 아님
        assertThat(result[1].userId).isEqualTo(follower2.id)
        assertThat(result[1].nickname).isEqualTo(follower2.nickname)
        assertThat(result[1].isFollowing).isFalse()
    }

    @Test
    @DisplayName("실패: 존재하지 않는 유저의 팔로워 목록 조회 시 USER_NOT_FOUND 예외가 발생한다")
    fun `존재하지 않는 유저 팔로워 목록 조회`() {
        // given
        val invalidUserId = 999
        whenever(userRepository.findById(invalidUserId)).thenReturn(Optional.empty())

        // when & then
        val e = assertThrows<CustomException> {
            followService.getFollowers(invalidUserId)
        }

        assertThat(e.errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
        verify(followRepository, never()).findFollowersByFolloweeIdAndStatus(any(), any())
    }

    @Test
    @DisplayName("성공: PENDING 상태의 팔로우 요청을 수락한다")
    fun `팔로우 요청 수락 성공`() {
        // given
        val followId = 1
        val follower = spy(User.createNormalUser("follower@test.com", "pw", "팔로워"))
        val followee = spy(User.createNormalUser("followee@test.com", "pw", "팔로이"))
        val pendingFollow = spy(Follow(follower, followee))

        // ✅ [수정] findById를 Mocking하고, 결과를 Optional.of()로 감싸서 반환
        whenever(followRepository.findById(followId)).thenReturn(Optional.of(pendingFollow))

        // when
        followService.acceptFollow(followId)

        // then
        verify(pendingFollow).accept()
        verify(follower).increaseFollowingsCount()
        verify(followee).increaseFollowersCount()
    }

    @Test
    @DisplayName("실패: 이미 수락된(ACCEPTED) 팔로우 요청을 다시 수락하면 예외가 발생한다")
    fun `이미 수락된 요청 재수락 시 실패`() {
        // given
        val followId = 1
        val acceptedFollow = Follow(mock(), mock()).apply { accept() }

        // ✅ [수정] findById를 Mocking하고, 결과를 Optional.of()로 감싸서 반환
        whenever(followRepository.findById(followId)).thenReturn(Optional.of(acceptedFollow))

        // when & then
        val e = assertThrows<CustomException> {
            followService.acceptFollow(followId)
        }
        assertThat(e.errorCode).isEqualTo(ErrorCode.FOLLOW_ALREADY_ACCEPTED)
    }

    @Test
    @DisplayName("실패: 이미 거절된(REJECTED) 팔로우 요청을 수락하면 예외가 발생한다")
    fun `이미 거절된 요청 수락 시 실패`() {
        // given
        val followId = 1
        val rejectedFollow = Follow(mock(), mock()).apply { reject() }

        // ✅ [수정] findById를 Mocking하고, 결과를 Optional.of()로 감싸서 반환
        whenever(followRepository.findById(followId)).thenReturn(Optional.of(rejectedFollow))

        // when & then
        val e = assertThrows<CustomException> {
            followService.acceptFollow(followId)
        }
        assertThat(e.errorCode).isEqualTo(ErrorCode.FOLLOW_ALREADY_REJECTED)
    }

    @Test
    @DisplayName("성공: PENDING 상태의 팔로우 요청을 거절한다")
    fun `팔로우 요청 거절 성공`() {
        // given: PENDING 상태인 Follow 객체가 존재한다고 가정
        val followId = 1
        val pendingFollow = spy(Follow(mock(), mock())) // reject() 메서드 호출을 감시하기 위해 spy 사용
        whenever(followRepository.findById(followId)).thenReturn(Optional.of(pendingFollow))

        // when: 팔로우 요청 거절 실행
        followService.rejectFollow(followId)

        // then: Follow 객체의 reject() 메서드가 정확히 1번 호출되었는지 검증
        verify(pendingFollow).reject()
    }

    @Test
    @DisplayName("실패: 이미 거절된(REJECTED) 팔로우 요청을 다시 거절하면 예외가 발생한다")
    fun `이미 거절된 요청 재거절 시 실패`() {
        // given: REJECTED 상태인 Follow 객체가 존재한다고 가정
        val followId = 1
        val rejectedFollow = Follow(mock(), mock()).apply { reject() }
        whenever(followRepository.findById(followId)).thenReturn(Optional.of(rejectedFollow))

        // when & then: 예외가 발생하는지 검증
        val e = assertThrows<CustomException> {
            followService.rejectFollow(followId)
        }
        assertThat(e.errorCode).isEqualTo(ErrorCode.FOLLOW_ALREADY_REJECTED)
    }

    @Test
    @DisplayName("실패: 이미 수락된(ACCEPTED) 팔로우 요청을 거절하면 예외가 발생한다")
    fun `이미 수락된 요청 거절 시 실패`() {
        // given: ACCEPTED 상태인 Follow 객체가 존재한다고 가정
        val followId = 1
        val acceptedFollow = Follow(mock(), mock()).apply { accept() }
        whenever(followRepository.findById(followId)).thenReturn(Optional.of(acceptedFollow))

        // when & then: 예외가 발생하는지 검증
        val e = assertThrows<CustomException> {
            followService.rejectFollow(followId)
        }
        assertThat(e.errorCode).isEqualTo(ErrorCode.FOLLOW_ALREADY_ACCEPTED)
    }

    @Test
    @DisplayName("실패: 존재하지 않는 followId를 거절하면 예외가 발생한다")
    fun `존재하지 않는 요청 거절 시 실패`() {
        // given: findById가 빈 Optional을 반환하도록 설정 (결과 없음)
        val followId = 999
        whenever(followRepository.findById(followId)).thenReturn(Optional.empty())

        // when & then
        val e = assertThrows<CustomException> {
            followService.rejectFollow(followId)
        }
        assertThat(e.errorCode).isEqualTo(ErrorCode.FOLLOW_NOT_FOUND)
    }

    @Test
    @DisplayName("성공: 내가 보낸 PENDING 상태의 요청 목록을 DTO로 변환하여 반환한다")
    fun `보낸 요청 목록 조회 성공`() {
        // given
        val userId = 1
        val user = mock<User>()
        whenever(user.id).thenReturn(userId)
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

        // ✅ DTO 생성에 필요한 모든 필드를 whenever로 설정해줍니다.
        val followee1 = mock<User>()
        whenever(followee1.id).thenReturn(2)
        whenever(followee1.nickname).thenReturn("상대방1")
        whenever(followee1.email).thenReturn("followee1@test.com") // 👈 이메일 설정 추가
        whenever(followee1.profileImageUrl).thenReturn(null)     // 👈 프로필 이미지 설정 추가

        val followee2 = mock<User>()
        whenever(followee2.id).thenReturn(3)
        whenever(followee2.nickname).thenReturn("상대방2")
        whenever(followee2.email).thenReturn("followee2@test.com") // 👈 이메일 설정 추가
        whenever(followee2.profileImageUrl).thenReturn("url/p2.jpg") // 👈 프로필 이미지 설정 추가

        // ✅ Follow 객체도 mock으로 만들어 followId와 followee를 제공합니다.
        val follow1 = mock<Follow>()
        val follow2 = mock<Follow>()
        whenever(follow1.id).thenReturn(101)
        whenever(follow1.followee).thenReturn(followee1)
        whenever(follow2.id).thenReturn(102)
        whenever(follow2.followee).thenReturn(followee2)

        val pendingRequests = listOf(follow1, follow2)
        whenever(followRepository.findSentPendingRequestsByFollowerIdAndStatus(userId, FollowStatus.PENDING))
            .thenReturn(pendingRequests)

        // when
        val result = followService.getSentPendingRequests(userId)

        // then
        assertThat(result).hasSize(2)
        assertThat(result).extracting("nickname")
            .containsExactlyInAnyOrder("상대방1", "상대방2")
        assertThat(result).extracting("email")
            .containsExactlyInAnyOrder("followee1@test.com", "followee2@test.com")
    }

    @Test
    @DisplayName("성공: 내가 보낸 PENDING 상태의 요청이 없으면 빈 리스트를 반환한다")
    fun `보낸 요청 목록이 없을 때`() {
        // given: 내가 보낸 PENDING 요청이 없는 상황을 가정
        val userId = 1
        // ✅ User 객체를 Mock으로 만들고, id의 행동을 미리 정의합니다.
        val user = mock<User>()
        whenever(user.id).thenReturn(userId)
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

        // Repository가 빈 리스트를 반환하는 상황을 흉내
        whenever(followRepository.findSentPendingRequestsByFollowerIdAndStatus(userId, FollowStatus.PENDING))
            .thenReturn(emptyList())

        // when: 보낸 요청 목록 조회 실행
        val result = followService.getSentPendingRequests(userId)

        // then: 반환된 리스트가 비어있는지 확인
        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("성공: 내가 받은 PENDING 상태의 요청 목록을 DTO로 변환하여 반환한다")
    fun `받은 요청 목록 조회 성공`() {
        // given: 내가 받은 PENDING 요청이 2개 있는 상황을 가정
        val userId = 1
        val user = mock<User>() // '나' 역할
        whenever(user.id).thenReturn(userId)
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

        // 나에게 요청을 보낸 상대방(follower)들
        val follower1 = mock<User>()
        whenever(follower1.id).thenReturn(2)
        whenever(follower1.nickname).thenReturn("요청자1")
        whenever(follower1.email).thenReturn("follower1@test.com")
        whenever(follower1.profileImageUrl).thenReturn(null)

        val follower2 = mock<User>()
        whenever(follower2.id).thenReturn(3)
        whenever(follower2.nickname).thenReturn("요청자2")
        whenever(follower2.email).thenReturn("follower2@test.com")
        whenever(follower2.profileImageUrl).thenReturn("url/p2.jpg")

        // Repository가 반환할 Follow 객체 목록 (mock으로 생성)
        val request1 = mock<Follow>()
        whenever(request1.id).thenReturn(101)
        whenever(request1.follower).thenReturn(follower1) // fromFollower는 follower 정보가 필요

        val request2 = mock<Follow>()
        whenever(request2.id).thenReturn(102)
        whenever(request2.follower).thenReturn(follower2)

        val pendingRequests = listOf(request1, request2)
        whenever(followRepository.findPendingRequestsByFolloweeIdAndStatus(userId, FollowStatus.PENDING))
            .thenReturn(pendingRequests)

        // when: 받은 요청 목록 조회 실행
        val result = followService.getPendingRequests(userId)

        // then:
        // 1. 반환된 DTO 리스트의 크기가 2인지 확인
        assertThat(result).hasSize(2)
        // 2. DTO의 내용이 GIVEN에서 만든 follower들의 정보와 일치하는지 확인
        assertThat(result).extracting("nickname")
            .containsExactlyInAnyOrder("요청자1", "요청자2")
        assertThat(result).extracting("userId")
            .containsExactlyInAnyOrder(2, 3)
    }

    @Test
    @DisplayName("성공: 내가 받은 PENDING 상태의 요청이 없으면 빈 리스트를 반환한다")
    fun `받은 요청 목록이 없을 때`() {
        // given: 내가 받은 PENDING 요청이 없는 상황
        val userId = 1
        val user = mock<User>()
        whenever(user.id).thenReturn(userId)
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        whenever(followRepository.findPendingRequestsByFolloweeIdAndStatus(userId, FollowStatus.PENDING))
            .thenReturn(emptyList()) // Repository가 빈 리스트를 반환

        // when
        val result = followService.getPendingRequests(userId)

        // then
        assertThat(result).isEmpty() // 반환된 리스트가 비어있는지 확인
    }
}