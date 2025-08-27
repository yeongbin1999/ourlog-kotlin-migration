package com.back.ourlog.domain.report.controller;

import com.back.ourlog.domain.report.dto.ReportRequest;
import com.back.ourlog.domain.report.entity.ReportReason;
import com.back.ourlog.domain.user.entity.Role;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.global.security.service.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("사용자 신고 요청 성공 테스트")
    @Test
    void reportUser_Success() throws Exception {
        User user = User.builder()
                .id(1)
                .email("reporter@test.com")
                .nickname("reporter")
                .role(Role.USER)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(userDetails, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ReportRequest request = new ReportRequest(
                2, // 신고 대상 사용자 ID
                ReportReason.SPAM, // 신고 유형 (enum 값)
                "스팸 메시지를 보냅니다." // 신고 내용
        );

        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(authentication)) // 인증된 사용자 정보 전달
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("SUCCESS_200"))
                .andExpect(jsonPath("$.msg").value("신고가 접수되었습니다."));
    }
}