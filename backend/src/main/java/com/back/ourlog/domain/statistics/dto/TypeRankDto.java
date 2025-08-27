package com.back.ourlog.domain.statistics.dto;

import com.back.ourlog.domain.content.entity.ContentType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TypeRankDto {
    private ContentType type;
    private Long totalCount;
}