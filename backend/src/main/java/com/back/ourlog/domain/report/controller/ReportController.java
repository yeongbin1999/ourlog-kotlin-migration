package com.back.ourlog.domain.report.controller;

import com.back.ourlog.domain.report.dto.ReportRequest;
import com.back.ourlog.domain.report.service.ReportService;
import com.back.ourlog.global.common.dto.RsData;
import com.back.ourlog.global.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public RsData<?> reportUser(@RequestBody ReportRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return reportService.reportUser(userDetails.getId(), request);
    }
}

