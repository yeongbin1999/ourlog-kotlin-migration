package com.back.ourlog.global.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,        // 실제 데이터 리스트
        int page,               // 현재 페이지 번호 (0부터 시작)
        int size,               // 한 페이지당 아이템 수
        long totalElements,     // 전체 데이터 수
        int totalPages,         // 전체 페이지 수 (계산 포함)
        boolean hasNext         // 다음 페이지 존재 여부
) {
    // Page<T> -> PageResponse<T> JPA가 기본으로 제공하는 페이징을 커스텀
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}

