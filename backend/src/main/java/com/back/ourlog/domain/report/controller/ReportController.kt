package com.back.ourlog.domain.report.controller

import com.back.ourlog.domain.report.dto.ReportRequest
import com.back.ourlog.domain.report.service.ReportService
import com.back.ourlog.global.common.dto.RsData
import com.back.ourlog.global.common.extension.toSuccessResponseWithoutData
import com.back.ourlog.global.security.service.CustomUserDetails
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reports")
class ReportController (
    private val reportService: ReportService
) {
    @PostMapping
    fun reportUser(
        @RequestBody request: ReportRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<RsData<Nothing>> {
        reportService.reportUser(userDetails.id, request)
        return toSuccessResponseWithoutData("신고 접수 성공")
    }
}

