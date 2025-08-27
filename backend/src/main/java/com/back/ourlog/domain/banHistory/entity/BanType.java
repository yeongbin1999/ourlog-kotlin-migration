package com.back.ourlog.domain.banHistory.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BanType {
    TEMPORARY("일정 기간 동안 임시 차단"),
    PERMANENT("영구 차단"),
    REPORT_ACCUMULATION("신고 누적"),
    ADMIN_DECISION("관리자 판단");

    private final String description;
}