package com.back.ourlog.global.common.dto

import org.springframework.data.domain.Page

data class PageResponse<T>(
    val content: List<T>,     // 실제 데이터 리스트
    val page: Int,            // 현재 페이지 번호 (0부터 시작)
    val size: Int,            // 한 페이지당 아이템 수
    val totalElements: Long,  // 전체 데이터 수
    val totalPages: Int,      // 전체 페이지 수
    val hasNext: Boolean      // 다음 페이지 존재 여부
) {
    companion object {
        fun <T> from(page: Page<T>): PageResponse<T> =
            PageResponse(
                content = page.content,
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                hasNext = page.hasNext()
            )
    }
}