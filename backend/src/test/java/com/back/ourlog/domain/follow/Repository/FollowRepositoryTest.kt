package com.back.ourlog.domain.follow.Repository

import com.back.ourlog.domain.follow.entity.Follow
import com.back.ourlog.domain.follow.enums.FollowStatus
import com.back.ourlog.domain.follow.repository.FollowRepository
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.domain.user.repository.UserRepository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest // JPA 관련 테스트를 위한 어노테이션
class FollowRepositoryTest {

    @Autowired
    private lateinit var followRepository: FollowRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var user1: User
    private lateinit var user2: User
    private lateinit var user3: User
    private lateinit var user4: User

    // 각 테스트가 실행되기 전에 테스트에 필요한 데이터를 미리 생성합니다.
    @BeforeEach
    fun setUp() {
        user1 = User.createNormalUser("user1@test.com", "password", "유저1", null, null)
        user2 = User.createNormalUser("user2@test.com", "password", "유저2", null, null)
        user3 = User.createNormalUser("user3@test.com", "password", "유저3", null, null)
        user4 = User.createNormalUser("user4@test.com", "password", "유저4", null, null)
        userRepository.saveAll(listOf(user1, user2, user3, user4))
    }

    @Test
    @DisplayName("팔로우 관계가 존재할 경우 Follow 객체 반환 성공")
    fun successfulFindByFollowerIdAndFolloweeId() {
        // given - 테스트 준비
        // user1이 user2를 팔로우하는 관계를 미리 저장합니다.
        val follow = Follow(follower = user1, followee = user2)
        followRepository.save(follow)

        // when - 테스트할 메서드 실행
        val result = followRepository.findByFollowerIdAndFolloweeId(user1.id!!, user2.id!!)

        // then - 결과 검증
        // 결과가 null이 아니어야 합니다.
        assertThat(result).isNotNull
        // 반환된 Follow 객체의 follower와 followee ID가 예상과 일치하는지 확인합니다.
        assertThat(result!!.follower.id).isEqualTo(user1.id)
        assertThat(result.followee.id).isEqualTo(user2.id)
    }

    @Test
    @DisplayName("팔로우 관계가 존재하지 않을 경우 null 반환 성공")
    fun failedFindByFollowerIdAndFolloweeId() {
        // given - 테스트 준비
        // 여기서는 아무런 Follow 관계도 저장하지 않습니다.

        // when - 테스트할 메서드 실행
        val result = followRepository.findByFollowerIdAndFolloweeId(user1.id!!, user2.id!!)

        // then - 결과 검증
        // 결과가 null이어야 합니다.
        assertThat(result).isNull()
    }

    @Test
    @DisplayName("내가 팔로우한(ACCEPTED) 유저 목록 조회 성공")
    fun successfulFindFollowingsByFollowerIdAndStatus() {
        // given - 테스트 준비 (user1의 팔로우 관계 설정)
        // 1. user1이 user2를 팔로우 (수락됨) -> ✅ 찾아야 함
        val follow1 = Follow(follower = user1, followee = user2).apply { accept() }
        // 2. user1이 user3를 팔로우 (수락됨) -> ✅ 찾아야 함
        val follow2 = Follow(follower = user1, followee = user3).apply { accept() }
        // 3. user1이 user4에게 팔로우 요청 (아직 PENDING) -> ❌ 무시해야 함
        val follow3 = Follow(follower = user1, followee = user4)
        // 4. user4가 user1을 팔로우 (수락됨, 역방향) -> ❌ 이건 내가 '팔로워'인 경우이므로 무시해야 함
        val follow4 = Follow(follower = user4, followee = user1).apply { accept() }
        followRepository.saveAll(listOf(follow1, follow2, follow3, follow4))

        // when - 테스트할 메서드 실행
        // user1을 기준으로 'ACCEPTED' 상태인 팔로잉 목록을 조회합니다.
        val result = followRepository.findFollowingsByFollowerIdAndStatus(user1.id!!, FollowStatus.ACCEPTED)

        // then - 결과 검증
        // 결과 리스트의 크기는 2여야 합니다.
        assertThat(result).hasSize(2)
        // 결과 리스트에 user2와 user3를 팔로우하는 관계가 정확히 포함되어 있는지 확인합니다.
        // (extracting을 사용하면 객체에서 특정 필드만 뽑아서 검증할 수 있어 편리합니다.)
        assertThat(result).extracting("followee")
            .extracting("nickname")
            .containsExactlyInAnyOrder("유저2", "유저3")
    }

    @Test
    @DisplayName("팔로우한(ACCEPTED) 유저가 없을 경우 빈 리스트 반환 성공")
    fun emptyFindFollowingsByFollowerIdAndStatus() {
        // given - 테스트 준비
        // user1이 user2에게 팔로우 요청만 하고 아직 수락되지 않은(PENDING) 상황
        val pendingFollow = Follow(follower = user1, followee = user2)
        followRepository.save(pendingFollow)

        // when - 테스트할 메서드 실행
        // user1을 기준으로 'ACCEPTED' 상태인 팔로잉 목록을 조회합니다.
        val result = followRepository.findFollowingsByFollowerIdAndStatus(user1.id!!, FollowStatus.ACCEPTED)

        // then - 결과 검증
        // 'ACCEPTED' 상태인 관계는 없으므로, 결과는 비어있는 리스트여야 합니다.
        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("나를 팔로우한(ACCEPTED) 유저 목록 조회 성공")
    fun successfulFindFollowersByFolloweeIdAndStatus() {
        // given - 테스트 준비 (user1을 팔로우하는 관계 설정)
        // 1. user2가 user1을 팔로우 (수락됨) -> ✅ 찾아야 함
        val follow1 = Follow(follower = user2, followee = user1).apply { accept() }
        // 2. user3가 user1을 팔로우 (수락됨) -> ✅ 찾아야 함
        val follow2 = Follow(follower = user3, followee = user1).apply { accept() }
        // 3. user4가 user1에게 팔로우 요청 (아직 PENDING) -> ❌ 무시해야 함
        val follow3 = Follow(follower = user4, followee = user1)
        // 4. user1이 user4를 팔로우 (수락됨, 역방향) -> ❌ 이건 내가 '팔로잉'하는 경우이므로 무시해야 함
        val follow4 = Follow(follower = user1, followee = user4).apply { accept() }
        followRepository.saveAll(listOf(follow1, follow2, follow3, follow4))

        // when - 테스트할 메서드 실행
        // user1을 기준으로 'ACCEPTED' 상태인 팔로워 목록을 조회합니다.
        val result = followRepository.findFollowersByFolloweeIdAndStatus(user1.id!!, FollowStatus.ACCEPTED)

        // then - 결과 검증
        // 결과 리스트의 크기는 2여야 합니다.
        assertThat(result).hasSize(2)
        // 결과 리스트에 user2와 user3가 나를 팔로우하는 관계가 정확히 포함되어 있는지 확인합니다.
        // 이번에는 'follower'의 닉네임을 확인해야 합니다.
        assertThat(result).extracting("follower")
            .extracting("nickname")
            .containsExactlyInAnyOrder("유저2", "유저3")
    }

    @Test
    @DisplayName("내가 보낸 팔로우 요청(PENDING) 목록 조회 성공")
    fun successfulFindSentPendingRequests() {
        // given - 테스트 준비 (user1이 보낸 다양한 상태의 요청 설정)
        // 1. user1이 user2에게 보낸 요청 (PENDING) -> ✅ 찾아야 함
        val request1 = Follow(follower = user1, followee = user2)
        // 2. user1이 user3에게 보낸 요청 (PENDING) -> ✅ 찾아야 함
        val request2 = Follow(follower = user1, followee = user3)
        // 3. user1이 user4에게 보낸 요청 (하지만 이미 ACCEPTED) -> ❌ 무시해야 함
        val request3 = Follow(follower = user1, followee = user4).apply { accept() }
        // 4. user2가 user1에게 보낸 요청 (PENDING, 역방향) -> ❌ 이건 내가 '받은' 요청이므로 무시해야 함
        val request4 = Follow(follower = user2, followee = user1)
        followRepository.saveAll(listOf(request1, request2, request3, request4))

        // when - 테스트할 메서드 실행
        // user1을 기준으로 'PENDING' 상태인 보낸 요청 목록을 조회합니다.
        val result = followRepository.findSentPendingRequestsByFollowerIdAndStatus(user1.id!!, FollowStatus.PENDING)

        // then - 결과 검증
        // 결과 리스트의 크기는 2여야 합니다.
        assertThat(result).hasSize(2)
        // 결과 리스트에 user2와 user3에게 보낸 요청이 정확히 포함되어 있는지 확인합니다.
        assertThat(result).extracting("followee")
            .extracting("nickname")
            .containsExactlyInAnyOrder("유저2", "유저3")
    }

    @Test
    @DisplayName("보낸 팔로우 요청(PENDING)이 없을 경우 빈 리스트 반환 성공")
    fun emptyFindSentPendingRequests() {
        // given - 테스트 준비
        // user1이 보낸 요청이 있지만, 이미 수락된(ACCEPTED) 상태인 경우
        val acceptedRequest = Follow(follower = user1, followee = user2).apply { accept() }
        followRepository.save(acceptedRequest)

        // when - 테스트할 메서드 실행
        // user1을 기준으로 'PENDING' 상태인 보낸 요청 목록을 조회합니다.
        val result = followRepository.findSentPendingRequestsByFollowerIdAndStatus(user1.id!!, FollowStatus.PENDING)

        // then - 결과 검증
        // 'PENDING' 상태인 요청은 없으므로, 결과는 비어있는 리스트여야 합니다.
        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("내가 받은 팔로우 요청(PENDING) 목록 조회 성공")
    fun successfulFindPendingRequests() {
        // given - 테스트 준비 (user1이 받은 다양한 상태의 요청 설정)
        // 1. user2가 user1에게 보낸 요청 (PENDING) -> ✅ 찾아야 함
        val request1 = Follow(follower = user2, followee = user1)
        // 2. user3가 user1에게 보낸 요청 (PENDING) -> ✅ 찾아야 함
        val request2 = Follow(follower = user3, followee = user1)
        // 3. user4가 user1에게 보낸 요청 (하지만 이미 ACCEPTED) -> ❌ 무시해야 함
        val request3 = Follow(follower = user4, followee = user1).apply { accept() }
        // 4. user1이 user2에게 보낸 요청 (PENDING, 역방향) -> ❌ 이건 내가 '보낸' 요청이므로 무시해야 함
        val request4 = Follow(follower = user1, followee = user2)
        followRepository.saveAll(listOf(request1, request2, request3, request4))

        // when - 테스트할 메서드 실행
        // user1을 기준으로 'PENDING' 상태인 받은 요청 목록을 조회합니다.
        val result = followRepository.findPendingRequestsByFolloweeIdAndStatus(user1.id!!, FollowStatus.PENDING)

        // then - 결과 검증
        // 결과 리스트의 크기는 2여야 합니다.
        assertThat(result).hasSize(2)
        // 결과 리스트에 user2와 user3에게서 받은 요청이 정확히 포함되어 있는지 확인합니다.
        // 이번에는 'follower'의 닉네임을 확인해야 합니다.
        assertThat(result).extracting("follower")
            .extracting("nickname")
            .containsExactlyInAnyOrder("유저2", "유저3")
    }

    @Test
    @DisplayName("받은 팔로우 요청(PENDING)이 없을 경우 빈 리스트 반환 성공")
    fun emptyFindPendingRequests() {
        // given - 테스트 준비
        // user2가 user1에게 요청을 보냈지만, 이미 수락된(ACCEPTED) 상태인 경우
        val acceptedRequest = Follow(follower = user2, followee = user1).apply { accept() }
        followRepository.save(acceptedRequest)

        // when - 테스트할 메서드 실행
        // user1을 기준으로 'PENDING' 상태인 받은 요청 목록을 조회합니다.
        val result = followRepository.findPendingRequestsByFolloweeIdAndStatus(user1.id!!, FollowStatus.PENDING)

        // then - 결과 검증
        // 'PENDING' 상태인 요청은 없으므로, 결과는 비어있는 리스트여야 합니다.
        assertThat(result).isEmpty()
    }
}