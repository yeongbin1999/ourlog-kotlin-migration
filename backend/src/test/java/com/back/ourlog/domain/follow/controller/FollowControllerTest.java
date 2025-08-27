package com.back.ourlog.domain.follow.controller;

import com.back.ourlog.domain.follow.entity.Follow;
import com.back.ourlog.domain.follow.enums.FollowStatus;
import com.back.ourlog.domain.follow.repository.FollowRepository;
import com.back.ourlog.domain.follow.service.FollowService;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FollowControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FollowService followService;

    private User user1;
    private User user2;

    @BeforeEach
    void setup() {
        followRepository.deleteAll();

        // user1은 @WithUserDetails가 자동으로 DB에 넣어주므로 조회만 함
        user1 = userRepository.findByEmail("user1@test.com")
                .orElseThrow(() -> new RuntimeException("user1이 DB에 없습니다"));

        // user2만 직접 저장
        user2 = userRepository.findByEmail("user2@test.com")
                .orElseGet(() -> userRepository.save(
                        User.createNormalUser(
                                "user2@test.com",
                                passwordEncoder.encode("1234"),
                                "테스터2",
                                null,
                                null
                        )
                ));
    }

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("팔로우 요청 성공")
    void t1_follow_success() throws Exception {
        mvc.perform(post("/api/v1/follows/" + user2.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("팔로우 요청했습니다."));
    }

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("언팔로우 요청 성공")
    void t2_unfollow_success() throws Exception {
        // Step 1: 팔로우 먼저
        mvc.perform(post("/api/v1/follows/" + user2.getId()))
                .andExpect(status().isOk());

        // Step 2: 언팔로우 요청
        mvc.perform(delete("/api/v1/follows/" + user2.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("팔로우 관계를 끊었습니다."));
    }

    @Test
    @WithUserDetails("user1@test.com")
    @DisplayName("팔로우한 유저 목록 조회 성공")
    void t2_get_followings_success() throws Exception {
        // 팔로우 요청
        mvc.perform(post("/api/v1/follows/" + user2.getId()))
                .andExpect(status().isOk());

        // 수락 (user2가 한 것처럼 시뮬레이션)
        Integer followId = followRepository
                .findAllByFollowerIdAndFolloweeId(user1.getId(), user2.getId())
                .get(0).getId();
        followService.acceptFollow(followId);

        // followings 조회
        mvc.perform(get("/api/v1/follows/followings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithUserDetails("user1@test.com") // ✅ user1 입장에서 "나를 팔로우한 유저" 확인
    @DisplayName("나를 팔로우한 유저 목록 조회 성공")
    void t3_getFollowers_success() throws Exception {
        // user2가 user1을 팔로우
        followService.follow(user2.getId(), user1.getId());

        // user1이 요청 수락
        Integer followId = followRepository
                .findAllByFollowerIdAndFolloweeId(user2.getId(), user1.getId())
                .get(0)
                .getId();

        followService.acceptFollow(followId);

        // ✅ user1이 자신의 팔로워 목록 조회
        mvc.perform(get("/api/v1/follows/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nickname").value("유저2")); // 🔁 user2가 팔로워니까 닉네임은 테스터2
    }

    @Test
    @WithUserDetails("user1@test.com") // user1이 팔로우 요청 수락할 유저
    @DisplayName("팔로우 요청 수락 성공")
    void t4_acceptFollow_success() throws Exception {
        // 1. user2 → user1 팔로우 요청
        followService.follow(user2.getId(), user1.getId());

        // 2. followId 얻기
        Integer followId = followRepository
                .findAllByFollowerIdAndFolloweeId(user2.getId(), user1.getId())
                .get(0)
                .getId();

        // 3. 수락 요청
        mvc.perform(post("/api/v1/follows/" + followId + "/accept"))
                .andExpect(status().isOk())
                .andExpect(content().string("팔로우 요청 수락 완료!"));

        // 4. 수락됐는지 검증 (OPTIONAL)
        assertEquals(FollowStatus.ACCEPTED,
                followRepository.findById(followId).get().getStatus());
    }

    @Test
    @WithUserDetails("user1@test.com") // ✅ user1이 로그인 -> 요청 "받은 사람"
    @DisplayName("팔로우 요청 거절 성공")
    void t5_rejectFollow_success() throws Exception {
        // ✅ user2가 user1에게 요청을 보냄
        followService.follow(user2.getId(), user1.getId());

        // 🔍 followId 가져오기
        Integer followId = followRepository
                .findAllByFollowerIdAndFolloweeId(user2.getId(), user1.getId())
                .get(0).getId();

        // ✅ user1이 로그인해서 거절
        mvc.perform(delete("/api/v1/follows/" + followId + "/reject"))
                .andExpect(status().isOk())
                .andExpect(content().string("팔로우 요청 거절 완료!"));

        // ✅ 상태 확인
        Follow follow = followRepository.findById(followId).orElseThrow();
        assertEquals(FollowStatus.REJECTED, follow.getStatus());
    }

    @Test
    @WithUserDetails("user1@test.com") // 유저1 로그인!
    @DisplayName("내가 보낸 팔로우 요청 목록 조회 성공")
    void t6_getSentRequests_success() throws Exception {
        // 1. user1이 user2에게 팔로우 요청 보냄
        followService.follow(user1.getId(), user2.getId());

        // 2. 유저1 기준으로 내가 보낸 요청 목록 조회
        mvc.perform(get("/api/v1/follows/sent-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)) // 1건 있어야 함
                .andExpect(jsonPath("$[0].email").value("user2@test.com"))
                .andExpect(jsonPath("$[0].nickname").value("유저2")); // 유저2 정보여야 함
    }


    @Test
    @WithUserDetails("user1@test.com") // 유저1 로그인 (요청 받는 사람)
    @DisplayName("내가 받은 팔로우 요청 목록 조회 성공")
    void t7_getPendingRequests_success() throws Exception {
        // 1. 유저2가 유저1을 팔로우 (요청 보냄)
        followService.follow(user2.getId(), user1.getId());

        // 2. 유저1이 받은 요청 목록 조회
        mvc.perform(get("/api/v1/follows/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)) // 요청 1건
                .andExpect(jsonPath("$[0].email").value("user2@test.com"))
                .andExpect(jsonPath("$[0].nickname").value("유저2")); // 유저2 정보
    }



}
