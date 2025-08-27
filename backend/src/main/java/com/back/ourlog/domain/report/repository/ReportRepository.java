package com.back.ourlog.domain.report.repository;

import com.back.ourlog.domain.report.entity.Report;
import com.back.ourlog.domain.report.entity.ReportReason;
import com.back.ourlog.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReportRepository extends JpaRepository<Report, Integer> {

    boolean existsByReporterAndTargetAndType(User reporter, User target, ReportReason type);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.target.id = :userId AND r.reportedAt >= :since")
    long countRecentReportsForUser(@Param("userId") Integer userId, @Param("since") LocalDateTime since);
}

