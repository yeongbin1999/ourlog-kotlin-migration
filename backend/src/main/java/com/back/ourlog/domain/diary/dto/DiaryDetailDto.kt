package com.back.ourlog.domain.diary.dto

import com.back.ourlog.domain.diary.entity.Diary
import java.io.Serializable

data class DiaryDetailDto(
    val title: String,
    val rating: Float,
    val contentText: String,
    val tagNames: List<String>,
    val genreNames: List<String>,
    val ottNames: List<String>,
) : Serializable {
    companion object {
        fun from(diary: Diary): DiaryDetailDto {
            val tagNames = diary.diaryTags.map { it.tag.name }
            val genreNames = diary.diaryGenres.map { it.genre.name }
            val ottNames = diary.diaryOtts.map { it.ott.name }
            return DiaryDetailDto(
                title = diary.title,
                rating = diary.rating,
                contentText = diary.contentText,
                tagNames = tagNames,
                genreNames = genreNames,
                ottNames = ottNames
            )
        }
    }
}
