package com.back.ourlog.domain.statistics.dto;

import com.back.ourlog.domain.content.entity.ContentType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
//@AllArgsConstructor
public class TypeLineGraphDto {
    private String axisLabel;        // “2025-03” 또는 “2025-03-14”
    private ContentType type;        // DRAMA, MOVIE, BOOK, MUSIC …
    private Long count;              // 일기 수

    // 임시 생성자 직접 작성
    public TypeLineGraphDto(String axisLabel, ContentType type, Long count) {
        this.axisLabel = axisLabel;
        this.type = type;
        this.count = count;
    }
}
