package com.back.ourlog.domain.genre.entity

import com.back.ourlog.domain.diary.entity.Diary
import java.io.Serializable

data class DiaryGenreId(
    var diary: Diary? = null,
    var genre: Genre? = null
) : Serializable
