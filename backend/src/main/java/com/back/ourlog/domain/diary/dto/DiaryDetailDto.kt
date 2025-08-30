package com.back.ourlog.domain.diary.dto

import com.back.ourlog.domain.diary.entity.Diary
import lombok.Data
import lombok.NoArgsConstructor
import java.io.Serializable

@Data
@NoArgsConstructor
class DiaryDetailDto(
    val title: String,
    val rating: Float,
    val contentText: String,
    val tagNames: List<String>,
    val genreNames: List<String>,
    val ottNames: List<String>,
) : Serializable {

    constructor(diary: Diary, tagNames: List<String>, genreNames: List<String>, ottNames: List<String>?) : this(
        title = diary.title,
        rating = diary.rating,
        contentText = diary.contentText,
        tagNames = tagNames,
        genreNames = genreNames,
        ottNames = ottNames ?: emptyList()
    )
    companion object {
        @JvmStatic
        fun of(diary: Diary): DiaryDetailDto {
            val tagNames = diary.diaryTags
                .map { it.tag.name }

            val genreNames = diary.diaryGenres
                .map { it.genre.name }

            val ottNames = diary.diaryOtts
                .map { it.ott.name }

            return DiaryDetailDto(diary, tagNames, genreNames, ottNames)
        }
    }
}
