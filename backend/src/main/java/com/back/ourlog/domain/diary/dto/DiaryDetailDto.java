package com.back.ourlog.domain.diary.dto;

import com.back.ourlog.domain.diary.entity.Diary;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class DiaryDetailDto implements Serializable {
    private String title;
    private float rating;
    private String contentText;
    private List<String> tagNames;
    private List<String> genreNames;
    private List<String> ottNames;

    public DiaryDetailDto(Diary diary, List<String> tagNames, List<String> genreNames, List<String> ottNames) {
        this.title = diary.getTitle();
        this.rating = diary.getRating();
        this.contentText = diary.getContentText();
        this.tagNames = tagNames;
        this.genreNames = genreNames;
        this.ottNames = ottNames != null ? ottNames : List.of();
    }

    public static DiaryDetailDto of(Diary diary) {
        List<String> tagNames = diary.getDiaryTags().stream()
                .map(dt -> dt.getTag().getName())
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
