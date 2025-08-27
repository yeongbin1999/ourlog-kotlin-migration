package com.back.ourlog.domain.report.service;

import com.back.ourlog.domain.banHistory.service.BanHistoryService;
import com.back.ourlog.domain.report.dto.ReportRequest;
import com.back.ourlog.domain.report.entity.Report;
import com.back.ourlog.domain.report.entity.ReportReason;
import com.back.ourlog.domain.report.repository.ReportRepository;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.domain.user.repository.UserRepository;
import com.back.ourlog.global.common.dto.RsData;
import com.back.ourlog.global.exception.CustomException;
import com.back.ourlog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final BanHistoryService banHistoryService;

    private static final int BAN_THRESHOLD = 5;
    private static final int BAN_DAYS = 7;

    public RsData<?> reportUser(Integer reporterId, ReportRequest request) {
        Integer targetUserId = request.targetUserId();
        ReportReason type = request.type();
        String description = request.description();

        if (reporterId.equals(targetUserId)) {
            throw new CustomException(ErrorCode.REPORT_SELF_NOT_ALLOWED);
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        boolean exists = reportRepository.existsByReporterAndTargetAndType(reporter, target, type);
        if (exists) {
            throw new CustomException(ErrorCode.REPORT_ALREADY_EXISTS);
        }

        Report report = Report.builder()
                .reporter(reporter)
                .target(target)
                .type(type)
                .description(description)
                .build();

        reportRepository.save(report);

        long recentReports = reportRepository.countRecentReportsForUser(
                target.getId(), LocalDateTime.now().minusDays(30));

        if (recentReports >= BAN_THRESHOLD) {
            banHistoryService.banUser(
                    target.getId(),
                    "신고 누적 " + recentReports + "건",
                    Duration.ofDays(BAN_DAYS)
            );
        }

        return RsData.success("신고가 접수되었습니다.");
    }
}
