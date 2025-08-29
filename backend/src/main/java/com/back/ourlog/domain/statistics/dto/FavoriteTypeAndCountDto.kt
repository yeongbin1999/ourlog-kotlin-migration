package com.back.ourlog.domain.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FavoriteTypeAndCountDto {
    private String favoriteType;
    private long favoriteTypeCount;
}
