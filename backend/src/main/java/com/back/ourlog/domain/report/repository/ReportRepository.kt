package com.back.ourlog.domain.report.repository

import com.back.ourlog.domain.report.entity.Report
import com.back.ourlog.domain.report.entity.ReportReason
import com.back.ourlog.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ReportRepository : JpaRepository<Report, Int> {
    fun existsByReporterAndTargetAndType(reporter: User, target: User, type: ReportReason): Boolean

    @Query("SELECT COUNT(r) FROM Report r WHERE r.target.id = :userId AND r.reportedAt >= :since")
    fun countRecentReportsForUser(@Param("userId") userId: Int, @Param("since") since: LocalDateTime): Long
}

