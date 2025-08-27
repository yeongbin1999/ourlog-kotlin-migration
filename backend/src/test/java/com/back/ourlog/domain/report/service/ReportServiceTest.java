package com.back.ourlog.domain.report.service;

import com.back.ourlog.domain.banHistory.service.BanHistoryService;
import com.back.ourlog.domain.report.dto.ReportRequest;
import com.back.ourlog.domain.report.entity.ReportReason;
import com.back.ourlog.domain.report.repository.ReportRepository;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.domain.user.repository.UserRepository;
import com.back.ourlog.global.common.dto.RsData;
import com.back.ourlog.global.exception.CustomException;
import com.back.ourlog.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    private ReportService reportService;

    private UserRepository userRepository;
    private ReportRepository reportRepository;
    private BanHistoryService banHistoryService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        reportRepository = mock(ReportRepository.class);
        banHistoryService = mock(BanHistoryService.class);

        reportService = new ReportService(userRepository, reportRepository, banHistoryService);
    }

    @Test
    @DisplayName("신고 성공: 정상 신고 접수 및 DB 저장, 밴 호출 없음")
    void reportUser_Success() {
        Integer reporterId = 1;
        Integer targetUserId = 2;

        ReportRequest request = new ReportRequest(targetUserId, ReportReason.SPAM, "스팸 신고");

        User reporter = new User();
        User target = new User();

        when(userRepository.findById(reporterId)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(target));
        when(reportRepository.existsByReporterAndTargetAndType(reporter, target, ReportReason.SPAM)).thenReturn(false);
        when(reportRepository.countRecentReportsForUser(eq(targetUserId), any(LocalDateTime.class))).thenReturn(0L);

        RsData<?> result = reportService.reportUser(reporterId, request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMsg()).isEqualTo("신고가 접수되었습니다.");

        // ReportRepository.save 호출 여부 확인
        verify(reportRepository, times(1)).save(any());

        // BanHistoryService.banUser 호출 안 함 (신고 5건 미만이므로)
        verify(banHistoryService, never()).banUser(anyInt(), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("신고 실패: 자기 자신을 신고할 경우 예외 발생")
    void reportUser_Fail_SelfReport() {
        Integer userId = 1;
        ReportRequest request = new ReportRequest(userId, ReportReason.SPAM, "자기 자신 신고");

        assertThatThrownBy(() -> reportService.reportUser(userId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REPORT_SELF_NOT_ALLOWED);
    }

    @Test
    @DisplayName("신고 실패: 신고자 정보가 없을 경우 예외 발생")
    void reportUser_Fail_UserNotFound() {
        Integer reporterId = 1;
        Integer targetUserId = 2;

        ReportRequest request = new ReportRequest(targetUserId, ReportReason.SPAM, "신고");

        when(userRepository.findById(reporterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.reportUser(reporterId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("신고 실패: 이미 신고한 대상일 경우 예외 발생")
    void reportUser_Fail_AlreadyReported() {
        Integer reporterId = 1;
        Integer targetUserId = 2;

        ReportRequest request = new ReportRequest(targetUserId, ReportReason.SPAM, "신고");

        User reporter = new User();
        User target = new User();

        when(userRepository.findById(reporterId)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(target));
        when(reportRepository.existsByReporterAndTargetAndType(reporter, target, ReportReason.SPAM)).thenReturn(true);

        assertThatThrownBy(() -> reportService.reportUser(reporterId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REPORT_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("신고 5건 누적 시 밴 처리 호출 및 성공 반환")
    void reportUser_BanUserCalled_WhenThresholdExceeded() {
        Integer reporterId = 1;
        Integer targetUserId = 2;

        ReportRequest request = new ReportRequest(targetUserId, ReportReason.SPAM, "신고");

        User reporter = new User();
        User target = new User();
        target.setId(targetUserId);

        when(userRepository.findById(reporterId)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(target));
        when(reportRepository.existsByReporterAndTargetAndType(reporter, target, ReportReason.SPAM)).thenReturn(false);
        when(reportRepository.countRecentReportsForUser(eq(targetUserId), any(LocalDateTime.class))).thenReturn(5L);

        RsData<?> result = reportService.reportUser(reporterId, request);

        verify(banHistoryService, times(1)).banUser(eq(targetUserId), contains("신고 누적"), eq(Duration.ofDays(7)));

        assertThat(result.isSuccess()).isTrue();
    }
}
