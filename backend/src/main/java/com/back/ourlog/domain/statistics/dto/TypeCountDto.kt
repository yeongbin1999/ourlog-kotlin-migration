package com.back.ourlog.domain.statistics.dto;

import com.back.ourlog.domain.content.entity.ContentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TypeCountDto {
    private String  type;
    private long count;
}
