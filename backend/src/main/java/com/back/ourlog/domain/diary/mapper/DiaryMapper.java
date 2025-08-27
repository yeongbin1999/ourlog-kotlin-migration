package com.back.ourlog.domain.diary.mapper;

import com.back.ourlog.domain.diary.dto.DiaryDetailDto;
import com.back.ourlog.domain.diary.dto.DiaryResponseDto;
import com.back.ourlog.domain.diary.entity.Diary;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DiaryMapper {

    public static DiaryResponseDto toResponseDto(Diary diary) {
        return DiaryResponseDto.from(diary);
    }

    public static DiaryDetailDto toDetailDto(Diary diary) {
        List<String> tagNames = diary.getDiaryTags().stream()
                .map(diaryTag -> diaryTag.getTag().getName())
                .toList();

        List<String> genreNames = diary.getDiaryGenres().stream()
                .map(dg -> dg.getGenre().getName())
                .toList();

        List<String> ottNames = diary.getDiaryOtts().stream()
                .map(doo -> doo.getOtt().getName())
                .toList();

        return new DiaryDetailDto(diary, tagNames, genreNames, ottNames);
    }
}
