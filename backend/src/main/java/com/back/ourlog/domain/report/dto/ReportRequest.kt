package com.back.ourlog.domain.report.dto

import com.back.ourlog.domain.report.entity.ReportReason
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ReportRequest(
    @field:NotNull
    val targetUserId: Int,

    @field:NotNull
    val type: ReportReason,

    @field:NotNull
    @field:Size(max = 255)
    val description: String
)
