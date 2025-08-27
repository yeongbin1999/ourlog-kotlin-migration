package com.back.ourlog.domain.follow.service;

import com.back.ourlog.domain.follow.dto.FollowUserResponse;
import com.back.ourlog.domain.follow.entity.Follow;
import com.back.ourlog.domain.follow.enums.FollowStatus;
import com.back.ourlog.domain.follow.repository.FollowRepository;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.domain.user.repository.UserRepository;
import com.back.ourlog.global.exception.CustomException;
import com.back.ourlog.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FollowServiceTest {

    @Autowired private FollowService followService;
    @Autowired private UserRepository userRepository;
    @Autowired private FollowRepository followRepository;

    @PersistenceContext // ✅ 이렇게 클래스 필드에 선언!
    private EntityManager em;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(User.createNormalUser(
                "a@test.com", "encodedPwA", "userA", null, null));
        userB = userRepository.save(User.createNormalUser(
                "b@test.com", "encodedPwB", "userB", null, null));
    }

    private void removeAllFollows(Integer userId1, Integer userId2) {
        followRepository.findAllByUsersEitherDirection(userId1, userId2)
                .forEach(followRepository::delete);
    }

    private Integer getFollowId(User follower, User followee) {
        return followRepository
                .findAllByFollowerIdAndFolloweeId(follower.getId(), followee.getId())
                .get(0).getId();
    }


    @Test
    @DisplayName("userA가 userB를 팔로우하면 상태는 PENDING이다")
    void followUser_createsPendingFollow() {
        followService.follow(userA.getId(), userB.getId());

        List<Follow> follows = followRepository.findAllByFollowerIdAndFolloweeId(userA.getId(), userB.getId());
        assertEquals(1, follows.size());
        assertEquals(FollowStatus.PENDING, follows.get(0).getStatus());
    }

    @Test
    @DisplayName("자기 자신을 팔로우하려고 하면 예외 발생")
    void followSelf_shouldThrowException() {
        CustomException e = assertThrows(CustomException.class, () ->
                followService.follow(userA.getId(), userA.getId()));
        assertEquals(ErrorCode.CANNOT_FOLLOW_SELF, e.getErrorCode());
    }

    @Test
    @DisplayName("팔로우 관계가 존재할 때 언팔로우하면 삭제되고, 카운트가 감소한다")
    void testUnfollow_success() {
        // given
        followService.follow(userA.getId(), userB.getId()); // PENDING 생성
        followService.acceptFollow( // 강제 수락 처리
                followRepository.findAllByFollowerIdAndFolloweeId(userA.getId(), userB.getId()).get(0).getId()
        );

        int beforeFollowings = userA.getFollowingsCount();
        int beforeFollowers = userB.getFollowersCount();

        // when
        followService.unfollow(userA.getId(), userB.getId());

        // then
        List<Follow> found = followRepository.findAllByUsersEitherDirection(userA.getId(), userB.getId());
        assertTrue(found.isEmpty());

        User refreshedFollower = userRepository.findById(userA.getId()).orElseThrow();
        User refreshedFollowee = userRepository.findById(userB.getId()).orElseThrow();

        assertEquals(beforeFollowings - 1, refreshedFollower.getFollowingsCount());
        assertEquals(beforeFollowers - 1, refreshedFollowee.getFollowersCount());
    }

    @DisplayName("팔로우 관계가 없으면 언팔로우 시도 시 예외가 발생한다")
    @Test
    @WithUserDetails("user1@test.com")
    void testUnfollow_notFound() {
        // given
        User a = userRepository.save(User.createNormalUser("c@test.com", "1234", "c", null, null));
        User b = userRepository.save(User.createNormalUser("d@test.com", "1234", "d", null, null));

        // when & then
        CustomException e = assertThrows(CustomException.class,
                () -> followService.unfollow(a.getId(), b.getId())
        );

        assertEquals(ErrorCode.FOLLOW_NOT_FOUND, e.getErrorCode());
    }

    @Test
    @DisplayName("역방향 PENDING 상태일 때 follow() 호출 시 자동 ACCEPT 처리된다")
    void follow_shouldAutoAccept_whenReversePendingExists() {
        // given
        Integer userAId = userA.getId();
        Integer userBId = userB.getId();

        followService.follow(userBId, userAId); // userB → userA (PENDING)
        followRepository.flush();

        Follow reversePending = followRepository
                .findAllByFollowerIdAndFolloweeId(userBId, userAId)
                .get(0);

        assertEquals(FollowStatus.PENDING, reversePending.getStatus());

        int beforeA_followings = userA.getFollowingsCount();
        int beforeB_followers = userB.getFollowersCount();

        // when: userA → userB (역방향 PENDING 존재 → 자동 ACCEPT)
        followService.follow(userAId, userBId);

        followRepository.flush();
        em.clear(); // 💡 영속성 컨텍스트 초기화

        // then: 상태는 ACCEPTED가 되어야 함
        Follow updated = followRepository
                .findById(reversePending.getId())
                .orElseThrow();

        assertEquals(FollowStatus.ACCEPTED, updated.getStatus());

        // 카운트 증가 확인
        User refreshedA = userRepository.findById(userAId).orElseThrow();
        User refreshedB = userRepository.findById(userBId).orElseThrow();

        assertEquals(beforeA_followings + 1, refreshedA.getFollowingsCount());
        assertEquals(beforeB_followers + 1, refreshedB.getFollowersCount());

        // follow 개수 1개만 존재해야 함
        List<Follow> allRelations = followRepository.findAllByFollowerIdAndFolloweeId(userBId, userAId);
        assertEquals(1, allRelations.size());
        assertEquals(reversePending.getId(), allRelations.get(0).getId());
    }

//    @Test
//    @DisplayName("역방향이 ACCEPTED 상태일 때 follow() 호출 없이도 쌍방 ACCEPTED 관계가 된다")
//    void acceptFollow_shouldCreateMutualAcceptedRelation() {
//        // given
//        Integer userAId = userA.getId();
//        Integer userBId = userB.getId();
//
//        // userB → userA 요청
//        followService.follow(userBId, userAId);
//        Follow request = followRepository
//                .findAllByFollowerIdAndFolloweeId(userBId, userAId)
//                .get(0);
//
//        int beforeA_followings = userA.getFollowingsCount();
//        int beforeB_followers = userB.getFollowersCount();
//
//        // when: userA가 요청을 수락
//        followService.acceptFollow(request.getId());
//        em.flush();
//        em.clear();
//
//        // then: 쌍방 ACCEPTED가 되어야 함
//        List<Follow> ab = followRepository.findAllByFollowerIdAndFolloweeId(userAId, userBId);
//        List<Follow> ba = followRepository.findAllByFollowerIdAndFolloweeId(userBId, userAId);
//
//        assertEquals(1, ab.size());
//        assertEquals(1, ba.size());
//
//        assertEquals(FollowStatus.ACCEPTED, ab.get(0).getStatus());
//        assertEquals(FollowStatus.ACCEPTED, ba.get(0).getStatus());
//
//        // 카운트 증가 확인
//        User refreshedA = userRepository.findById(userAId).orElseThrow();
//        User refreshedB = userRepository.findById(userBId).orElseThrow();
//
//        assertEquals(beforeA_followings + 1, refreshedA.getFollowingsCount());
//        assertEquals(beforeB_followers + 1, refreshedB.getFollowersCount());
//    }

    @Test
    @DisplayName("이미 수락된 follow에 대해 acceptFollow() 호출 시 예외 발생")
    void acceptFollow_shouldThrow_whenAlreadyAccepted() {
        // given: userB → userA follow 요청
        followService.follow(userB.getId(), userA.getId());
        Follow follow = followRepository
                .findAllByFollowerIdAndFolloweeId(userB.getId(), userA.getId())
                .get(0);

        // 첫 번째 수락 (정상)
        followService.acceptFollow(follow.getId());

        // when & then: 두 번째 수락 시도 → 예외 발생해야 함
        CustomException exception = assertThrows(CustomException.class, () ->
                followService.acceptFollow(follow.getId())
        );

        assertEquals(ErrorCode.FOLLOW_ALREADY_ACCEPTED, exception.getErrorCode());
    }

    @Test
    @DisplayName("followId 기반으로 follow를 거절하면 상태가 REJECTED가 된다")
    void rejectFollow_shouldSetStatusToRejected() {
        // given
        followService.follow(userB.getId(), userA.getId());
        Follow follow = followRepository
                .findAllByFollowerIdAndFolloweeId(userB.getId(), userA.getId())
                .get(0);

        // when
        followService.rejectFollow(follow.getId());

        // then
        Follow updated = followRepository.findById(follow.getId()).orElseThrow();
        assertEquals(FollowStatus.REJECTED, updated.getStatus());
    }

    @Test
    @DisplayName("이미 거절된 follow에 대해 rejectFollow() 호출 시 예외 발생")
    void rejectFollow_shouldThrow_whenAlreadyRejected() {
        // given
        followService.follow(userB.getId(), userA.getId());
        Follow follow = followRepository
                .findAllByFollowerIdAndFolloweeId(userB.getId(), userA.getId())
                .get(0);

        followService.rejectFollow(follow.getId()); // 첫 번째 거절

        // when & then
        CustomException exception = assertThrows(CustomException.class, () ->
                followService.rejectFollow(follow.getId())
        );

        assertEquals(ErrorCode.FOLLOW_ALREADY_REJECTED, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미 수락된 follow에 대해 rejectFollow() 호출 시 예외 발생")
    void rejectFollow_shouldThrow_whenAlreadyAccepted() {
        // given
        followService.follow(userB.getId(), userA.getId());
        Follow follow = followRepository
                .findAllByFollowerIdAndFolloweeId(userB.getId(), userA.getId())
                .get(0);

        followService.acceptFollow(follow.getId()); // 먼저 수락

        // when & then
        CustomException exception = assertThrows(CustomException.class, () ->
                followService.rejectFollow(follow.getId())
        );

        assertEquals(ErrorCode.FOLLOW_ALREADY_ACCEPTED, exception.getErrorCode());
    }

    @Test
    @DisplayName("getFollowings()는 내가 ACCEPTED한 유저 목록만 반환한다")
    void getFollowings_shouldReturnAcceptedFollowees() {
        // given
        User userC = userRepository.save(User.createNormalUser("c@test.com", "pw", "userC", null, null));
        User userD = userRepository.save(User.createNormalUser("d@test.com", "pw", "userD", null, null));

        // 🧹 반드시 모든 관계 제거
        removeAllFollows(userA.getId(), userB.getId());
        removeAllFollows(userA.getId(), userC.getId());
        removeAllFollows(userA.getId(), userD.getId());

        // follow & accept
        followService.follow(userA.getId(), userB.getId());
        followService.acceptFollow(getFollowId(userA, userB));

        followService.follow(userA.getId(), userC.getId());
        followService.acceptFollow(getFollowId(userA, userC));

        followService.follow(userA.getId(), userD.getId()); // PENDING

        // when
        var result = followService.getFollowings(userA.getId());

        // then
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("getFollowers()는 나를 ACCEPTED한 유저 목록만 반환한다")
    void getFollowers_shouldReturnAcceptedFollowers() {
        // given
        User userC = userRepository.save(User.createNormalUser("c@test.com", "pw", "userC", null, null));
        User userD = userRepository.save(User.createNormalUser("d@test.com", "pw", "userD", null, null));

        // 정리: userC, userD와 관계 초기화
        removeAllFollows(userC.getId(), userA.getId());
        removeAllFollows(userD.getId(), userA.getId());

        // userC → userA 팔로우 (ACCEPTED)
        followService.follow(userC.getId(), userA.getId());
        followService.acceptFollow(getFollowId(userC, userA));

        // userD → userA 팔로우 (PENDING)
        followService.follow(userD.getId(), userA.getId());

        // when
        var followers = followService.getFollowers(userA.getId());

        // then
        assertEquals(1, followers.size());
        assertEquals("userC", followers.get(0).getNickname());
    }

    @Test
    @DisplayName("getSentPendingRequests()는 내가 보낸 PENDING 요청만 반환한다")
    void getSentPendingRequests_shouldReturnOnlyPendingRequests() {
        // given
        User userC = userRepository.save(User.createNormalUser("c@test.com", "pw", "userC", null, null));
        User userD = userRepository.save(User.createNormalUser("d@test.com", "pw", "userD", null, null));

        // 🧹 모든 기존 관계 제거
        removeAllFollows(userA.getId(), userB.getId());
        removeAllFollows(userA.getId(), userC.getId());
        removeAllFollows(userA.getId(), userD.getId());

        // userA → userB : PENDING
        followService.follow(userA.getId(), userB.getId());

        // userA → userC : ACCEPTED
        followService.follow(userA.getId(), userC.getId());
        followService.acceptFollow(getFollowId(userA, userC));

        // userA → userD : PENDING
        followService.follow(userA.getId(), userD.getId());

        // when
        var result = followService.getSentPendingRequests(userA.getId());

        // then
        assertEquals(2, result.size());

        var nicknames = result.stream().map(FollowUserResponse::getNickname).toList();
        assertTrue(nicknames.contains("userB"));
        assertTrue(nicknames.contains("userD"));
    }

    @Test
    @DisplayName("getPendingRequests()는 내가 받은 PENDING 요청만 반환한다")
    void getPendingRequests_shouldReturnOnlyPendingReceivedRequests() {
        // given
        User userC = userRepository.save(User.createNormalUser("c@test.com", "pw", "userC", null, null));
        User userD = userRepository.save(User.createNormalUser("d@test.com", "pw", "userD", null, null));

        // 🧹 관계 초기화
        removeAllFollows(userB.getId(), userA.getId());
        removeAllFollows(userC.getId(), userA.getId());
        removeAllFollows(userD.getId(), userA.getId());

        // userB → userA : PENDING
        followService.follow(userB.getId(), userA.getId());

        // userC → userA : ACCEPTED
        followService.follow(userC.getId(), userA.getId());
        followService.acceptFollow(getFollowId(userC, userA));

        // userD → userA : PENDING
        followService.follow(userD.getId(), userA.getId());

        // when
        var result = followService.getPendingRequests(userA.getId());

        // then
        assertEquals(2, result.size());

        var nicknames = result.stream().map(FollowUserResponse::getNickname).toList();
        assertTrue(nicknames.contains("userB"));
        assertTrue(nicknames.contains("userD"));
    }

}
