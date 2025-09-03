package com.back.ourlog.domain.report.service

import com.back.ourlog.domain.banHistory.service.BanHistoryService
import com.back.ourlog.domain.report.dto.ReportRequest
import com.back.ourlog.domain.report.entity.Report
import com.back.ourlog.domain.report.repository.ReportRepository
import com.back.ourlog.domain.user.repository.UserRepository
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Service
@Transactional
class ReportService(
    private val userRepository: UserRepository,
    private val reportRepository: ReportRepository,
    private val banHistoryService: BanHistoryService
) {

    fun reportUser(reporterId: Int, request: ReportRequest): Boolean {
        // 1. 요청 필드 체크
        val targetUserId = request.targetUserId
        val type = request.type
        val description = request.description

        // 2. 자기 자신 신고 금지
        if (reporterId == targetUserId) {
            throw CustomException(ErrorCode.REPORT_SELF_NOT_ALLOWED)
        }

        // 3. reporter, target 조회
        val reporter = userRepository.findById(reporterId)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        val target = userRepository.findById(targetUserId)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        // 4. 중복 신고 확인
        if (reportRepository.existsByReporterAndTargetAndType(reporter, target, type)) {
            throw CustomException(ErrorCode.REPORT_ALREADY_EXISTS)
        }

        // 5. 신고 기록 저장
        val report = Report(
            reporter = reporter,
            target = target,
            type = type,
            description = description
        )
        reportRepository.save(report)

        // 6. 최근 30일간 신고 누적 확인 후 차단 처리
        val recentReports = reportRepository.countRecentReportsForUser(
            target.id, LocalDateTime.now().minusDays(30)
        )

        if (recentReports >= BAN_THRESHOLD) {
            try {
                banHistoryService.banUser(
                    target.id,
                    "신고 누적 $recentReports 건",
                    Duration.ofDays(BAN_DAYS.toLong())
                )
            } catch (ex: CustomException) {
                if (ex.errorCode != ErrorCode.BAN_ALREADY_EXISTS) {
                    throw ex
                }
                // 이미 차단된 경우 무시
            }
        }

        return true
    }

    companion object {
        private const val BAN_THRESHOLD = 5
        private const val BAN_DAYS = 7
    }
}