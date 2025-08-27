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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager em;

    // 팔로우..
    @Transactional
    public void follow(Integer followerId, Integer followeeId) {
        if (followerId.equals(followeeId)) {
            throw new CustomException(ErrorCode.CANNOT_FOLLOW_SELF);
        }

        // 기존 Follow 있으면 처리..
        List<Follow> existingList = followRepository.findAllByFollowerIdAndFolloweeId(followerId, followeeId);
        for (Follow f : existingList) {
            if (f.getStatus() == FollowStatus.ACCEPTED || f.getStatus() == FollowStatus.PENDING) {
                throw new CustomException(ErrorCode.FOLLOW_ALREADY_EXISTS);
            } else {
                followRepository.delete(f); // 이전 REJECTED 같은 건 삭제..
            }
        }

        // 역방향 PENDING 상태가 있다면 자동 수락..
        Optional<Follow> reversePending = followRepository.findByFollowerIdAndFolloweeIdAndStatus(
                followeeId, followerId, FollowStatus.PENDING);

        if (reversePending.isPresent()) {
            Follow reverse = reversePending.get();
            reverse.accept();
            reverse.getFollower().increaseFollowingsCount();
            reverse.getFollowee().increaseFollowersCount();
            followRepository.save(reverse);

            // ✅ forward 관계도 추가 (나 → 상대방)
            User follower = userRepository.findById(followerId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            User followee = userRepository.findById(followeeId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            Follow forward = new Follow(follower, followee);
            forward.accept();
            followRepository.save(forward);

            follower.increaseFollowingsCount();
            followee.increaseFollowersCount();

            return;
        }



        // 역방향이 ACCEPTED인 경우 → 쌍방으로 만들어야 함..
        Optional<Follow> reverseAccepted = followRepository.findByFollowerIdAndFolloweeIdAndStatus(
                followeeId, followerId, FollowStatus.ACCEPTED);

        if (reverseAccepted.isPresent()) {
            User follower = userRepository.findById(followerId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            User followee = userRepository.findById(followeeId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            Follow follow = new Follow(follower, followee);
            follow.accept();
            followRepository.save(follow);

            follower.increaseFollowingsCount();
            followee.increaseFollowersCount();
            return;
        }

        // 아무 관계도 없으면 새로 PENDING 생성..
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User followee = userRepository.findById(followeeId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Follow follow = new Follow(follower, followee); // 상태는 기본 PENDING..
        followRepository.save(follow);

        follower.increaseFollowingsCount();
        followee.increaseFollowersCount();
    }


    // 언팔로우..
    @Transactional
    public void unfollow(Integer myUserId, Integer otherUserId) {
        List<Follow> follows = followRepository.findAllByUsersEitherDirection(myUserId, otherUserId);

        if (follows.isEmpty()) {
            throw new CustomException(ErrorCode.FOLLOW_NOT_FOUND);
        }

        for (Follow follow : follows) {
            followRepository.delete(follow);
            follow.getFollower().decreaseFollowingsCount();
            follow.getFollowee().decreaseFollowersCount();
        }

        followRepository.flush();
        em.clear();
    }

    // 내가 팔로우한 유저 목록 조회..
    public List<FollowUserResponse> getFollowings(Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Follow> followings = followRepository.findFollowingsByUserId(userId);

        return followings.stream()
                .map(f -> FollowUserResponse.fromFollowee(f, true))
                .toList();
    }


    // 나를 팔로우한 유저 목록 조회..
    public List<FollowUserResponse> getFollowers(Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Follow> follows = followRepository.findFollowersByUserId(userId);

        return follows.stream()
                .map(f -> {
                    Integer followerId = f.getFollower().getId(); // 여기에 선언..
                    boolean isFollowing = followRepository
                            .findByFollowerIdAndFolloweeIdAndStatus(userId, followerId, FollowStatus.ACCEPTED)
                            .isPresent();

                    System.out.println("[FOLLOWERS DEBUG] userId = " + userId +
                            ", followerId = " + followerId +
                            ", isFollowing = " + isFollowing);

                    return FollowUserResponse.fromFollower(f, isFollowing);
                })
                .toList();
    }


    // 팔로우 요청을 수락 상태로 변경..
    @Transactional
    public void acceptFollow(Integer followId) {
        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLLOW_NOT_FOUND));

        if (follow.getStatus() == FollowStatus.ACCEPTED) {
            throw new CustomException(ErrorCode.FOLLOW_ALREADY_ACCEPTED);
        }

        if (follow.getStatus() == FollowStatus.REJECTED) {
            throw new CustomException(ErrorCode.FOLLOW_ALREADY_REJECTED);
        }

        follow.accept();

        followRepository.save(follow);
        followRepository.flush();

    }

    // 팔로우 요청을 거절 상태로 변경..
    @Transactional
    public void rejectFollow(Integer followId) {
        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLLOW_NOT_FOUND));

        if (follow.getStatus() == FollowStatus.REJECTED) {
            throw new CustomException(ErrorCode.FOLLOW_ALREADY_REJECTED);
        }

        if (follow.getStatus() == FollowStatus.ACCEPTED) {
            throw new CustomException(ErrorCode.FOLLOW_ALREADY_ACCEPTED);
        }

        follow.reject();
    }

    // 내가 보낸 팔로우 요청 목록 (PENDING 상태)..
    public List<FollowUserResponse> getSentPendingRequests(Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Follow> sentRequests = followRepository.findSentPendingRequestsByUserId(userId);

        return sentRequests.stream()
                .map(FollowUserResponse::fromFollowee)
                .toList();
    }

    // 내가 받은 팔로우 요청 목록 (PENDING 상태)..
    public List<FollowUserResponse> getPendingRequests(Integer userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Follow> pendingFollows = followRepository.findPendingRequestsByUserId(userId);

        return pendingFollows.stream()
                .map(FollowUserResponse::fromFollower)
                .toList();
    }
}
