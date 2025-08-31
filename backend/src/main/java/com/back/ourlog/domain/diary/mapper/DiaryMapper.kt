package com.back.ourlog.domain.diary.mapper

import com.back.ourlog.domain.diary.dto.DiaryDetailDto
import com.back.ourlog.domain.diary.dto.DiaryResponseDto
import com.back.ourlog.domain.diary.dto.DiaryResponseDto.Companion.from
import com.back.ourlog.domain.diary.entity.Diary

object DiaryMapper {
    @JvmStatic
    fun toResponseDto(diary: Diary): DiaryResponseDto {
        return from(diary)
    }

    @JvmStatic
    fun toDetailDto(diary: Diary): DiaryDetailDto {
        val tagNames = diary.diaryTags.map { it.tag.name }
        val genreNames = diary.diaryGenres.map { it.genre.name }
        val ottNames = diary.diaryOtts.map { it.ott.name }

        return DiaryDetailDto(diary, tagNames, genreNames, ottNames)
    }
}
