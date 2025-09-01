package com.back.ourlog.domain.diary.mapper

import com.back.ourlog.domain.diary.dto.DiaryDetailDto
import com.back.ourlog.domain.diary.dto.DiaryResponseDto
import com.back.ourlog.domain.diary.entity.Diary

fun Diary.toResponseDto(): DiaryResponseDto = DiaryResponseDto.from(this)
fun Diary.toDetailDto(): DiaryDetailDto = DiaryDetailDto.from(this)
