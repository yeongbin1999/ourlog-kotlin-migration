package com.back.ourlog.domain.banHistory.entity

enum class BanType (
    private val description: String
) {
    TEMPORARY("일정 기간 동안 임시 차단"),
    PERMANENT("영구 차단"),
    REPORT_ACCUMULATION("신고 누적"),
    ADMIN_DECISION("관리자 판단");
}