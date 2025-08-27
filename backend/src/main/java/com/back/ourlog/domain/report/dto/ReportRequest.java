package com.back.ourlog.domain.report.dto;

import com.back.ourlog.domain.report.entity.ReportReason;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;

public record ReportRequest(
        @NotNull Integer targetUserId,
        @NotNull ReportReason type,
        @NotNull @Column(length = 255) String description
) {}
